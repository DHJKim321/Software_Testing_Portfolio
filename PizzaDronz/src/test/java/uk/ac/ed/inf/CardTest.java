package uk.ac.ed.inf;

import org.junit.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CardTest {

    @Test
    public void testCvvWithLengthNot3Or4() {
        Card card = new Card("", "", "12345");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testCvvNonNumeric() {
        Card card = new Card("", "", "fff");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testNotPassedValidExpiryDate() {
        Card card = new Card("", "10/23", "");
        assertTrue(card.checkDate());
    }

    @Test
    public void testExpiryDateSameYearAndMonth() {
        Card card = new Card("", YearMonth.now().format(DateTimeFormatter.ofPattern("MM/yy")), "");
        assertTrue(card.checkDate());
    }

    @Test
    public void testPastExpiryDate() {
        Card card = new Card("", "09/22", "");
        assertFalse(card.checkDate());
    }

    @Test
    public void testInvalidExpiryDate() {
        Card card = new Card("", "0922", "");
        assertFalse(card.checkDate());
    }

    @Test
    public void testLengthNot15Or16() {
        Card card = new Card("1", "", "");
        assertFalse(card.checkCardNumber());
    }

    @Test
    public void testAmexInvalidCvv() {
        Card card = new Card("379521561277409", "10/23", "313");
        assertFalse(card.checkCvv());
    }

    @Test
    public void testValidCvvNonAmexPassesLuhnCard() {
        Card card = new Card("5109768232423286", "10/23", "344");
        assertTrue(card.checkCardNumber());
    }

    @Test
    public void testValidCvvNonAmexNotPassesLuhnCard() {
        Card card = new Card("2191346386371338", "10/23", "344");
        assertFalse(card.checkCardNumber());
    }

    @Test
    public void testValidCvvAmexPassesLuhnCard() {
        Card card = new Card("379521561277409", "10/23", "3444");
        assertTrue(card.checkCardNumber());
    }

    @Test
    public void testValidCvvAmexNotPassesLuhnCard() {
        Card card = new Card("371449635398432", "10/23", "3444");
        assertFalse(card.checkCardNumber());
    }

    @Test
    public void testCardNumberNot15Or16() {
        Card card = new Card("11", "", "");
        assertFalse(card.checkCardNumber());
    }

    @Test
    public void testStartingStartingNumber0() {
        Card card = new Card("0", "", "");
        assertFalse(card.checkCardNumber());
    }

    @Test
    public void testDoesNotPassLuhnAlgorithm() {
        Card card = new Card("4417234567891143", "", "");
        assertFalse(card.checkCardNumber());
    }
}
