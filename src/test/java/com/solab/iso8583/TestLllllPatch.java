package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Extended test for LLLLVar field
 *
 * @author Lekan Omotayo
 */
@RunWith(Parameterized.class)
public class TestLllllPatch {

    private MessageFactory<IsoMessage> mfact = new MessageFactory<>();
    private int fieldLength;

    @Parameterized.Parameters
    public static List<Integer> lengths() {
        return Arrays.asList(5, 50, 500, 5000, 50000);
    }

    public TestLllllPatch(int l) {
        fieldLength = l;
    }

    @Before
    public void setup() throws IOException {
        mfact.setConfigPath("issue102.xml");
        mfact.setAssignDate(false);
    }

    @Test
    public void testParsingLength() throws Exception {
        // prepare
        String lllllvar = makeLLLLLVar(fieldLength);
        StringBuilder sb = new StringBuilder();
        sb.append("01004000000000000000")
                .append(String.format("%04d", fieldLength)).append(lllllvar);

        // parse
        IsoMessage m = mfact.parseMessage(sb.toString().getBytes(), 0);
        Assert.assertNotNull(m);
        String f2 = m.getObjectValue(2);
        Assert.assertEquals(lllllvar, f2);
        Assert.assertEquals(fieldLength, f2.length());
        //Encode
        m = mfact.newMessage(0x100);
        m.setIsoHeader(null);
        m.setValue(2, lllllvar, IsoType.LLLLLVAR, 0);
        Assert.assertEquals(sb.toString(), m.debugString());
    }

    @Test
    public void testSerialiseParseSmall() throws Exception {
        testSerialiseParse(88);
    }

    @Test
    public void testSerialiseParseMedium() throws Exception {
        testSerialiseParse(258);
    }

    @Test
    public void testSerialiseParseLarge() throws Exception {
        testSerialiseParse(99919);
    }

    private void testSerialiseParse(final int LENGTH) throws Exception {
        // prepare
        String LLLLLVar = makeLLLLLVar(LENGTH);
        IsoMessage m = mfact.newMessage(0x100);
        m.setValue(2, LLLLLVar, IsoType.LLLLLVAR, 0);
        m.setBinary(true);

        // write
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        m.write(bout, 2);
        bout.close();

        // read
        byte[] buf = new byte[2];
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        bin.read(buf);
        Assert.assertNotEquals(buf, new byte[2]);

        int len = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff); // modify here
        buf = new byte[len];
        bin.read(buf);
        bin.close();

        // parse
        mfact.setUseBinaryMessages(true);
        m = mfact.parseMessage(buf, mfact.getIsoHeader(0x100).length());
        Assert.assertNotNull(m);
        Assert.assertEquals(LLLLLVar, m.getObjectValue(2));
    }

    private String makeLLLLLVar(final int length) {
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = 'a';
        }

        return new String(chars);
    }
}
