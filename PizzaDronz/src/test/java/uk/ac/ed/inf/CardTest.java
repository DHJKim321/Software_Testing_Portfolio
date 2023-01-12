package uk.ac.ed.inf;

import org.junit.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CardTest {

    @Test
    public void testValidCardNumber() {
        Card card = new Card("4855200933750832", "", "");
        assertTrue(card.checkCardNumber());
    }

    @Test
    public void testLength15CardNumber() {
        Card card = new Card("371449635398431", "", "");
        assertTrue(card.checkCardNumber());
    }

    @Test
    public void testInvalidCardNumber() {
        Card card = new Card("111111111111111", "", "");
        assertFalse(card.checkCardNumber());
    }

    @Test
    public void testLength4CvvWithNonNumeric() {
        Card card = new Card("", "", "113f");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testCvvWithMoreThan4Numbers() {
        Card card = new Card("", "", "12345");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testValidCvv() {
        Card card = new Card("", "", "499");
        assertTrue(card.checkCvv());
    }

    @Test
    public void testLength3CvvWithAlphabet() {
        Card card = new Card("", "", "45f");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testCvvWithNonAlphanumeric() {
        Card card = new Card("", "", "!@.");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testValidExpiryDateNextYear() {
        Card card = new Card("", "10/23", "");
        assertTrue(card.checkDate());
    }

    @Test
    public void testValidExpiryDateSameYearAndMonth() {
        Card card = new Card("", YearMonth.now().format(DateTimeFormatter.ofPattern("MM/yy")), "");
        assertTrue(card.checkDate());
    }

    @Test
    public void testInvalidExpiryDate() {
        Card card = new Card("", "09/22", "");
        assertFalse(card.checkDate());
    }

    @Test
    public void testAmexInvalidCvv() {
        Card card = new Card("378282246310005", "10/23", "123");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testInvalidCardDate() {
        Card card = new Card("378282246310005", "12-02", "4124");
        assertFalse(card.checkDate());
    }

}
