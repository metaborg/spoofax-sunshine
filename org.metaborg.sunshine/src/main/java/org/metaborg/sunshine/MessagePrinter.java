package org.metaborg.sunshine;

import org.metaborg.core.build.ConsoleBuildMessagePrinter;
import org.metaborg.core.build.IBuildMessagePrinter;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

public class MessagePrinter {
    private static final ILogger logger = LoggerUtils.logger(MessagePrinter.class);

    private final ISourceTextService sourceTextService;


    @Inject public MessagePrinter(ISourceTextService sourceTextService) {
        this.sourceTextService = sourceTextService;
    }


    public IBuildMessagePrinter printer() {
        return new ConsoleBuildMessagePrinter(sourceTextService, true, true, logger);
    }

    public void print(Iterable<IMessage> messages) {
        final IBuildMessagePrinter printer = printer();
        for(IMessage message : messages) {
            printer.print(message, false);
        }
        printer.printSummary();
    }
}
