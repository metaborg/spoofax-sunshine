/**
 * 
 */
package org.spoofax.sunshine.framework.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.sunshine.model.messages.ARegion;
import org.spoofax.sunshine.model.messages.Message;
import org.spoofax.sunshine.model.messages.MessageSeverity;
import org.spoofax.sunshine.model.messages.MessageType;
import org.spoofax.sunshine.model.messages.PositionRegion;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MessageTest {

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#type()}.
	 */
	@Test
	public void testType1() {
		final Message msg = new Message();
		assertNull(msg.type());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#type()}.
	 */
	@Test
	public void testType2() {
		final Message msg = new Message();
		msg.type = MessageType.PARSER_MESSAGE;
		assertEquals(MessageType.PARSER_MESSAGE, msg.type());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#type()}.
	 */
	@Test
	public void testType3() {
		final Message msg = new Message();
		msg.type = MessageType.ANALYSIS_MESSAGE;
		assertEquals(MessageType.ANALYSIS_MESSAGE, msg.type());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#severity()}.
	 */
	@Test
	public void testSeverity1() {
		final Message msg = new Message();
		assertNull(msg.severity);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#severity()}.
	 */
	@Test
	public void testSeverity2() {
		final Message msg = new Message();
		msg.severity = MessageSeverity.ERROR;
		assertEquals(MessageSeverity.ERROR, msg.severity());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#file()}.
	 */
	@Test
	public void testFile1() {
		final Message msg = new Message();
		assertNull(msg.file());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#file()}.
	 */
	@Test
	public void testFile2() {
		final Message msg = new Message();
		msg.file = "foo";
		assertEquals("foo", msg.file());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#message()}.
	 */
	@Test
	public void testMessage1() {
		final Message msg = new Message();
		assertNull(msg.message());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#message()}.
	 */
	@Test
	public void testMessage2() {
		final Message msg = new Message();
		msg.msg = "foo";
		assertEquals("foo", msg.message());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#getAttachedException()} .
	 */
	@Test
	public void testGetAttachedException1() {
		final Message msg = new Message();
		assertNull(msg.getAttachedException());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#getAttachedException()} .
	 */
	@Test
	public void testGetAttachedException2() {
		final Message msg = new Message();
		final Exception ex = new RuntimeException("foobar");
		msg.exception = ex;
		assertEquals(ex, msg.exception);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject1() {
		final Message msg1 = new Message();
		final Message msg2 = new Message();
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject2() {
		final Message msg1 = new Message();
		msg1.type = MessageType.PARSER_MESSAGE;
		final Message msg2 = new Message();
		msg2.type = MessageType.PARSER_MESSAGE;
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject3() {
		final Message msg1 = new Message();
		msg1.severity = MessageSeverity.WARNING;
		final Message msg2 = new Message();
		msg2.severity = MessageSeverity.WARNING;
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject4() {
		final Message msg1 = new Message();
		msg1.file = "foobar";
		final Message msg2 = new Message();
		msg2.file = "foobar";
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject5() {
		final Message msg1 = new Message();
		msg1.msg = "foobar";
		final Message msg2 = new Message();
		msg2.msg = "foobar";
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject6() {
		final Message msg1 = new Message();
		msg1.exception = new RuntimeException("ex");
		final Message msg2 = new Message();
		msg2.exception = new RuntimeException("ex");
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject7() {
		final ARegion reg = new ARegion() {

			@Override
			public String toString() {
				return null;
			}

			@Override
			public int hashCode() {
				return 0;
			}

			@Override
			public boolean equals(Object o) {
				return true;
			}
		};
		final Message msg1 = new Message();
		msg1.region = reg;
		final Message msg2 = new Message();
		msg2.region = reg;
		assertEquals(msg1, msg2);
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject8() {
		final Message msg1 = new Message();
		msg1.type = MessageType.PARSER_MESSAGE;
		final Message msg2 = new Message();
		msg2.type = MessageType.ANALYSIS_MESSAGE;
		assertFalse(msg1.equals(msg2));
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject9() {
		final Message msg1 = new Message();
		msg1.severity = MessageSeverity.WARNING;
		final Message msg2 = new Message();
		msg2.severity = MessageSeverity.ERROR;
		assertFalse(msg1.equals(msg2));
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject10() {
		final Message msg1 = new Message();
		msg1.file = "foobarZ";
		final Message msg2 = new Message();
		msg2.file = "foobarz";
		assertFalse(msg1.equals(msg2));
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject11() {
		final Message msg1 = new Message();
		msg1.msg = "foobarZ";
		final Message msg2 = new Message();
		msg2.msg = "foobarz";
		assertFalse(msg1.equals(msg2));
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject12() {
		final Message msg1 = new Message();
		msg1.exception = new RuntimeException("exZ");
		final Message msg2 = new Message();
		msg2.exception = new RuntimeException("exz");
		assertFalse(msg1.equals(msg2));
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject13() {
		final ARegion reg = new ARegion() {

			@Override
			public String toString() {
				return null;
			}

			@Override
			public int hashCode() {
				return 0;
			}

			@Override
			public boolean equals(Object o) {
				return false;
			}
		};
		final Message msg1 = new Message();
		msg1.region = reg;
		final Message msg2 = new Message();
		msg2.region = reg;
		assertFalse(msg1.equals(msg2));
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#region()}.
	 */
	@Test
	public void testRegion1() {
		final Message msg1 = new Message();
		assertNull(msg1.region());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#region()}.
	 */
	@Test
	public void testRegion2() {
		final ARegion reg = new ARegion() {

			@Override
			public String toString() {
				return "";
			}

			@Override
			public int hashCode() {
				return 42;
			}

			@Override
			public boolean equals(Object o) {
				return true;
			}
		};
		final Message msg1 = new Message();
		msg1.region = reg;
		assertEquals(reg, msg1.region());
		assertEquals(42, msg1.region().hashCode());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#hashCode()}.
	 */
	@Test
	public void testHashCode1() {
		final Message msg1 = new Message();
		final Message msg2 = new Message();
		assertEquals(msg1.hashCode(), msg2.hashCode());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#hashCode()}.
	 */
	@Test
	public void testHashCode2() {
		final Message msg1 = new Message();
		msg1.file = "foobar";
		final Message msg2 = new Message();
		msg2.file = "foobar";
		assertEquals(msg1.hashCode(), msg2.hashCode());
	}

	/**
	 * Test method for {@link org.spoofax.sunshine.model.messages.Message#hashCode()}.
	 */
	@Test
	public void testHashCode3() {
		final Message msg1 = new Message();
		msg1.file = "foobarZ";
		final Message msg2 = new Message();
		msg2.file = "foobarz";
		assertFalse(msg1.hashCode() == msg2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.messages.Message#newParseError(java.lang.String, org.spoofax.jsglr.client.imploder.IToken, org.spoofax.jsglr.client.imploder.IToken, java.lang.String)}
	 * .
	 */
	@Test
	public void testNewParseError() {
		final Message msg = Message.newParseError("foobar", tok1, tok2, "foobarz");
		assertEquals(MessageType.PARSER_MESSAGE, msg.type());
		assertEquals(MessageSeverity.ERROR, msg.severity());
		assertEquals("foobar", msg.file());
		assertEquals("foobarz", msg.message());
		assertNotNull(msg.region());
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.messages.Message#newParseWarning(java.lang.String, org.spoofax.jsglr.client.imploder.IToken, org.spoofax.jsglr.client.imploder.IToken, java.lang.String)}
	 * .
	 */
	@Test
	public void testNewParseWarning() {
		final Message msg = Message.newParseWarning("foobar", tok1, tok2, "foobarz");
		assertEquals(MessageType.PARSER_MESSAGE, msg.type());
		assertEquals(MessageSeverity.WARNING, msg.severity());
		assertEquals("foobar", msg.file());
		assertEquals("foobarz", msg.message());
		assertNotNull(msg.region());
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.messages.Message#newParseErrorAtTop(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testNewParseErrorAtTop() {
		final Message msg = Message.newParseErrorAtTop("foobar", "foobarz");
		assertEquals(MessageType.PARSER_MESSAGE, msg.type());
		assertEquals(MessageSeverity.ERROR, msg.severity());
		assertEquals("foobar", msg.file());
		assertEquals("foobarz", msg.message());
		assertEquals(new PositionRegion(0, 0, 1, 0), msg.region());
	}

	/**
	 * Test method for
	 * {@link org.spoofax.sunshine.model.messages.Message#newParseWarningAtTop(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testNewParseWarningAtTop() {
		final Message msg = Message.newParseWarningAtTop("foobar", "foobarz");
		assertEquals(MessageType.PARSER_MESSAGE, msg.type());
		assertEquals(MessageSeverity.WARNING, msg.severity());
		assertEquals("foobar", msg.file());
		assertEquals("foobarz", msg.message());
		assertEquals(new PositionRegion(0, 0, 1, 0), msg.region());
	}

	final IToken tok1 = new IToken() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3952104732492908939L;

		@Override
		public IToken clone() {
			return null;
		}

		@Override
		public int compareTo(IToken o) {
			return 0;
		}

		@Override
		public void setKind(int arg0) {

		}

		@Override
		public ITokenizer getTokenizer() {
			return null;
		}

		@Override
		public int getStartOffset() {
			return 0;
		}

		@Override
		public int getLine() {
			return 0;
		}

		@Override
		public int getLength() {
			return 0;
		}

		@Override
		public int getKind() {
			return 0;
		}

		@Override
		public int getIndex() {
			return 0;
		}

		@Override
		public String getError() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getEndOffset() {
			return 0;
		}

		@Override
		public int getEndLine() {
			return 0;
		}

		@Override
		public int getEndColumn() {
			return 0;
		}

		@Override
		public int getColumn() {
			return 0;
		}

		@Override
		public ISimpleTerm getAstNode() {
			return null;
		}

		@Override
		public char charAt(int arg0) {
			return 43;
		}
	};

	final IToken tok2 = new IToken() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3952104732492908938L;

		@Override
		public IToken clone() {
			return null;
		}

		@Override
		public int compareTo(IToken o) {
			return 0;
		}

		@Override
		public void setKind(int arg0) {

		}

		@Override
		public ITokenizer getTokenizer() {
			return null;
		}

		@Override
		public int getStartOffset() {
			return 0;
		}

		@Override
		public int getLine() {
			return 0;
		}

		@Override
		public int getLength() {
			return 0;
		}

		@Override
		public int getKind() {
			return 0;
		}

		@Override
		public int getIndex() {
			return 0;
		}

		@Override
		public String getError() {
			return null;
		}

		@Override
		public int getEndOffset() {
			return 0;
		}

		@Override
		public int getEndLine() {
			return 0;
		}

		@Override
		public int getEndColumn() {
			return 0;
		}

		@Override
		public int getColumn() {
			return 0;
		}

		@Override
		public ISimpleTerm getAstNode() {
			return null;
		}

		@Override
		public char charAt(int arg0) {
			return 42;
		}
	};

}
