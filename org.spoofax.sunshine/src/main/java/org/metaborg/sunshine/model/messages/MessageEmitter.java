package org.metaborg.sunshine.model.messages;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.metaborg.sunshine.services.messages.MessageSink;

public class MessageEmitter {
    private final MessageSink messager;
    private final ISourceTextService sourceTextService;

    public MessageEmitter(MessageSink messager, ISourceTextService sourceTextService) {
        this.messager = messager;
        this.sourceTextService = sourceTextService;
    }

    public void emitMessages(PrintStream os) {
        Collection<IMessage> messages = messager.getMessages();
        int i = 0;
        for(IMessage msg : messages) {
            try {
                os.println(i + ". " + print(msg, sourceTextService.text(msg.source())));
            } catch(IOException e) {
                os.println(i + ". " + msg);
            }
            i++;
        }
    }

    public void emitSummary(PrintStream os) {
        Collection<IMessage> messages = messager.getMessages();
        MessageSeverity[] severities = MessageSeverity.values();
        int[] counts = new int[severities.length];
        int total = 0;
        for(IMessage msg : messages) {
            int severity = 0;
            for(; severity < severities.length; severity++) {
                if(severities[severity] == msg.severity())
                    break;
            }
            counts[severity]++; // = counts[severity] + 1;
            total++;
        }
        os.print(total + " messages (");
        for(int severity = 0; severity < severities.length; severity++) {
            os.print(counts[severity] + " ");
            os.print(severities[severity]);
            if(severity < severities.length - 1) {
                os.print(", ");
            }
        }
        os.println(")");
    }

    public boolean hasErrors() {
        return hasMessage(MessageSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return hasMessage(MessageSeverity.WARNING);
    }

    public boolean hasNotes() {
        return hasMessage(MessageSeverity.NOTE);
    }

    private boolean hasMessage(MessageSeverity severity) {
        for(IMessage message : messager.getMessages()) {
            if(message.severity() == severity) {
                return true;
            }
        }
        return false;
    }

    private static String print(IMessage message, String sourceText) {
        final StringBuilder sb = new StringBuilder();
        sb.append(message.severity());
        if(message.source() != null) {
            sb.append(" in ");
            sb.append(message.source().getName().getPath());
            sb.append(" (at line " + message.region().startRow() + ")\n");
        } else {
            sb.append(" at line " + message.region().startRow() + "\n");
        }
        sb.append(AffectedSourceHelper.affectedSourceText(message.region(), sourceText, "\t"));
        sb.append(message);
        sb.append("\n");
        if(message.exception() != null) {
            sb.append("\tCaused by:\n");
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            message.exception().printStackTrace(pw);
            sb.append(sw.toString());
        }
        sb.append("----------");
        return sb.toString();
    }
}
