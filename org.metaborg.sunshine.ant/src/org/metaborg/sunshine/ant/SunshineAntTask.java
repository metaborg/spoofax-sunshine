/**
 * 
 */
package org.metaborg.sunshine.ant;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.metaborg.sunshine.ant.control.DependencyEvaluator;
import org.metaborg.sunshine.drivers.Main;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;
import org.metaborg.sunshine.services.messages.MessageService;

/**
 * @author vladvergu
 * 
 */
public class SunshineAntTask extends Task {

	File languageRepository;

	List<DependencyAntType> dependencies = new ArrayList<>();

	public void setLanguagerepository(File languageRepository) {
		this.languageRepository = languageRepository;
	}

	public void add(DependencyAntType dependency) {
		this.dependencies.add(dependency);
	}

	private static final Logger logger = LogManager
			.getLogger(SunshineAntTask.class.getName());

	@Override
	public void execute() throws BuildException {
		logger.trace("Initializing Sunshine using {} as language repository",
				languageRepository.getAbsolutePath());
		SunshineMainArguments params = new SunshineMainArguments();
		params.autolang = languageRepository.getAbsolutePath();
		params.project = getProject().getBaseDir().getAbsolutePath();
		params.validate();

		ServiceRegistry env = ServiceRegistry.INSTANCE();
		Main.initServices(env, params);

		env.getService(LanguageDiscoveryService.class).discover(
				FileSystems.getDefault().getPath(params.autolang));

		logger.trace("Sunshine initialized");
		DependencyEvaluator evaluator = new DependencyEvaluator();
		for (DependencyAntType dependency : dependencies) {
			evaluator.addDependency(dependency.toActionableDependency());
		}
		boolean evaluationResult = evaluator.evaluateDependencies();

		logger.trace("Evaluation finished. Emitting messages");

		MessageService msgService = ServiceRegistry.INSTANCE().getService(
				MessageService.class);
		msgService.emitMessages(System.out);
		msgService.emitSummary(System.out);
		if (!evaluationResult) {
			logger.fatal("Evaluation failed");
			throw new BuildException("Evaluation failed");
		}
	}

}
