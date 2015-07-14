package org.metaborg.sunshine.services.pipelined.builders;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.sunshine.environment.LaunchConfiguration;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.pipeline.ILinkManyToOne;
import org.metaborg.sunshine.pipeline.ISinkOne;
import org.metaborg.sunshine.pipeline.diff.Diff;
import org.metaborg.sunshine.pipeline.diff.MultiDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class BuilderInputTermFactoryLink implements
    ILinkManyToOne<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, BuilderInputTerm> {

    private static final Logger logger = LoggerFactory.getLogger(BuilderInputTermFactoryLink.class.getName());

    private final Collection<ISinkOne<BuilderInputTerm>> sinks = new HashSet<ISinkOne<BuilderInputTerm>>();

    private final FileObject path;

    private boolean onSource;

    private boolean ignoreErrors;

    public BuilderInputTermFactoryLink(FileObject file, boolean onSource, boolean ignoreErrors) {
        this.path = file;
        this.onSource = onSource;
        this.ignoreErrors = ignoreErrors;
    }

    @Override public void addSink(ISinkOne<BuilderInputTerm> sink) {
        assert sink != null;
        sinks.add(sink);
    }

    @Override public void sink(MultiDiff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> product) {
        assert product != null;
        logger.trace("Creating builder input term for product");
        Diff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> select = null;
        for(Diff<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> diff : product) {
            if(diff.getPayload().source.getName().getPath().equals(path.getName().getPath())) {
                select = diff;
                break;
            } else {
                logger
                    .trace("Input file {} does not match prebaked file {}, skipping.", diff.getPayload().source, path);
            }
        }
        if(select != null) {
            boolean errors_exist = false;
            for(IMessage msg : select.getPayload().messages) {
                if(msg.severity() == MessageSeverity.ERROR) {
                    errors_exist = true;
                    break;
                }
            }
            if(!errors_exist || ignoreErrors) {
                logger.trace("Selected file {} for creating input", select.getPayload().source);

                IStrategoTerm ast =
                    onSource && select.getPayload().previous != null ? select.getPayload().previous.result
                        : select.getPayload().result;
                LaunchConfiguration launch = ServiceRegistry.INSTANCE().getService(LaunchConfiguration.class);
                BuilderInputTerm payload =
                    new BuilderInputTerm(launch.termFactory, ast, select.getPayload().source, launch.projectDir);
                Diff<BuilderInputTerm> result = new Diff<BuilderInputTerm>(payload, select.getDiffKind());
                for(ISinkOne<BuilderInputTerm> sink : sinks) {
                    logger.trace("Sinking input term for file {} to builder {}", path, sink);
                    sink.sink(result);
                }
            } else {
                logger.info("Builder is skipping because of previous errors");
            }
        } else {
            logger.trace("No file in result matched the prebaked file {}", path);
        }
    }

}
