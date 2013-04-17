/**
 * 
 */
package org.spoofax.sunshine.services.parser;

import java.io.File;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.sunshine.model.messages.IMessage;
import org.spoofax.sunshine.parser.model.IStrategoParseOrAnalyzeResult;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class JSGLRParseResult implements IStrategoParseOrAnalyzeResult {

    private File file;
    private IStrategoTerm ast;
    private Collection<IMessage> messages;

    public JSGLRParseResult(File f) {
	this.file = f;
    }

    public void setAst(IStrategoTerm ast) {
	this.ast = ast;
    }

    public void setMessages(Collection<IMessage> messages) {
	this.messages = messages;
    }

    @Override
    public File file() {
	return file;
    }

    @Override
    public IStrategoTerm ast() {
	return ast;
    }

    @Override
    public Collection<IMessage> messages() {
	return messages;
    }

}
