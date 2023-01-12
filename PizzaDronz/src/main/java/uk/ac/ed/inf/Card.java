package uk.ac.ed.inf;


import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * This record contains card information and methods to verity them.
 */
public record Card(String creditCardNumber,
                   String date,
                   String cvv) {

    /**
     * This is a static factory method for creating new card objects.
     *
     * @param creditCardNumber Credit card number (15/16 digits).
     * @param date             Expiry date in the format YY/mm
     * @param cvv              CVV (3/4 digits)
     * @return A new card object with the above attributes.
     */
    public static Card createCard(String creditCardNumber, String date, String cvv) {
        return new Card(creditCardNumber, date, cvv);
    }

    /**
     * This method checks whether the card's cvv is valid
     * Requirements: 3 digit (4 if the card is from American Express) numbers
     *
     * @return Boolean value on whether the cvv is valid or not.
     */
    public boolean checkCvv() {
        if (creditCardNumber.startsWith("37") || creditCardNumber.startsWith("34")) { // AMEX cvv format
            if (cvv.length() != 4) {
                return false;
            }
        }
        if (cvv.length() != 3) { // Everything else
            return false;
        }
        return (cvv.matches("[0-9]+"));
    }

    /**
     * This method checks whether the card's expiry date is valid
     * Requirements: Expiry date is either the same month and year as the current date or after.
     *
     * @return Boolean value on whether the expiry date is valid or not.
     */
    public boolean checkDate() {
        try {
            var formatter = DateTimeFormatter.ofPattern("MM/yy");
            var now = YearMonth.now().format(formatter);
            var yearMonth = YearMonth.parse(date, formatter);
            return now.equals(yearMonth.format(formatter)) || YearMonth.now().isBefore(yearMonth);
        } catch (DateTimeParseException e) {
            System.err.println("The expiry date is in an invalid format.");
            return false;
        }
    }

    /**
     * This method checks whether the card number is valid
     * Requirements: If it passes the Luhn Algorithm, then it is a valid card number.
     *
     * @return Boolean value on whether the card number is valid or not.
     * Reference: <a href="https://en.wikipedia.org/wiki/Luhn_algorithm">...</a>
     */
    public boolean checkCardNumber() {
        var sum = 0;
        var parity = creditCardNumber.length() % 2;
        for (var i = 0; i < creditCardNumber.length(); i++) {
            var digit = Character.getNumericValue(creditCardNumber.charAt(i));
            if (i % 2 == parity) {
                digit *= 2;
            }
            var d1 = digit / 10; // If digit is < 9, then d1 = 0. Otherwise d1 = 1.
            var d2 = digit % 10;
            sum += d1 + d2; // Add the two digits together.
        }
        return (sum % 10 == 0);
    }

    @Override
    public String toString() {
        return "Card{" +
                "cvv='" + cvv + '\'' +
                ", date='" + date + '\'' +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                '}';
    }
}
