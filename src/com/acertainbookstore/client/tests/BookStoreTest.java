package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.acertainbookstore.business.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link BookStoreTest} tests the {@link BookStore} interface.
 * 
 * @see BookStore
 */
public class BookStoreTest {

	/** The Constant TEST_ISBN. */
	private static final int TEST_ISBN = 3044560;

	/** The Constant NUM_COPIES. */
	private static final int NUM_COPIES = 5;

	/** The local test. */
	private static boolean localTest = true;

	/** The store manager. */
	private static StockManager storeManager;

	/** The client. */
	private static BookStore client;

	/**
	 * Sets the up before class.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
			localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;

			if (localTest) {
				CertainBookStore store = new CertainBookStore();
				storeManager = store;
				client = store;
			} else {
				storeManager = new StockManagerHTTPProxy("http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}

			storeManager.removeAllBooks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to add some books.
	 *
	 * @param isbn
	 *            the isbn
	 * @param copies
	 *            the copies
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public void addBooks(int isbn, int copies) throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book = new ImmutableStockBook(isbn, "Test of Thrones", "George RR Testin'", (float) 10, copies, 0, 0,
				0, false);
		booksToAdd.add(book);
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Helper method to get the default book used by initializeBooks.
	 *
	 * @return the default book
	 */
	public StockBook getDefaultBook() {
		return new ImmutableStockBook(TEST_ISBN, "Harry Potter and JUnit", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,
				false);
	}

	/**
	 * Method to add a book, executed before every test case is run.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Before
	public void initializeBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(getDefaultBook());
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Method to clean up the book store, execute after every test case is run.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@After
	public void cleanupBooks() throws BookStoreException {
		storeManager.removeAllBooks();
	}

	/**
	 * Tests basic buyBook() functionality.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyAllCopiesDefaultBook() throws BookStoreException {
		// Set of books to buy
		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES));

		// Try to buy books
		client.buyBooks(booksToBuy);

		List<StockBook> listBooks = storeManager.getBooks();
		assertTrue(listBooks.size() == 1);
		StockBook bookInList = listBooks.get(0);
		StockBook addedBook = getDefaultBook();

		assertTrue(bookInList.getISBN() == addedBook.getISBN() && bookInList.getTitle().equals(addedBook.getTitle())
				&& bookInList.getAuthor().equals(addedBook.getAuthor()) && bookInList.getPrice() == addedBook.getPrice()
				&& bookInList.getNumSaleMisses() == addedBook.getNumSaleMisses()
				&& bookInList.getAverageRating() == addedBook.getAverageRating()
				&& bookInList.getNumTimesRated() == addedBook.getNumTimesRated()
				&& bookInList.getTotalRating() == addedBook.getTotalRating()
				&& bookInList.isEditorPick() == addedBook.isEditorPick());
	}

	/**
	 * Tests that books with invalid ISBNs cannot be bought.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyInvalidISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with invalid ISBN.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(-1, 1)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that books can only be bought if they are in the book store.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyNonExistingISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with ISBN which does not exist.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(100000, 10)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that you can't buy more books than there are copies.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyTooManyBooks() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy more copies than there are in store.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES + 1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that you can't buy a negative number of books.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyNegativeNumberOfBookCopies() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a negative number of copies.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that all books can be retrieved.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetBooks() throws BookStoreException {
		Set<StockBook> booksAdded = new HashSet<StockBook>();
		booksAdded.add(getDefaultBook());

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));

		booksAdded.addAll(booksToAdd);

		storeManager.addBooks(booksToAdd);

		// Get books in store.
		List<StockBook> listBooks = storeManager.getBooks();

		// Make sure the lists equal each other.
		assertTrue(listBooks.containsAll(booksAdded) && listBooks.size() == booksAdded.size());
	}

	/**
	 * Tests that a list of books with a certain feature can be retrieved.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetCertainBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));

		storeManager.addBooks(booksToAdd);

		// Get a list of ISBNs to retrieved.
		Set<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN + 1);
		isbnList.add(TEST_ISBN + 2);

		// Get books with that ISBN.
		List<Book> books = client.getBooks(isbnList);

		// Make sure the lists equal each other
		assertTrue(books.containsAll(booksToAdd) && books.size() == booksToAdd.size());
	}

	/**
	 * Tests that books cannot be retrieved if ISBN is invalid.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetInvalidIsbn() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Make an invalid ISBN.
		HashSet<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN); // valid
		isbnList.add(-1); // invalid

		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.getBooks(isbnList);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 *  Test that a book with a valid ISBN and valid rating can be rated.
	 *
	 */
	@Test
	public void testRateBookValidISBNValidRating() throws BookStoreException {
		// Create a rating for a given book
		Set<BookRating> booksToRate = new HashSet<>();
		booksToRate.add(new BookRating(TEST_ISBN, 4));

		// Add the rating to the store
		client.rateBooks(booksToRate);

		// Get current books in store
		List<StockBook> listBooks = storeManager.getBooks();
		StockBook bookInList = listBooks.get(0);

		// Test whether the rating is set. I.e. rating == 4
		assertEquals(bookInList.getTotalRating(), 4);
	}


	/**
	 *  Test that a book with a valid ISBN and invalid rating.
	 *	We expect a bookstore exception
	 *
	 */
	@Test(expected = BookStoreException.class)
	public void testRateBookValidISBNInvalidRating() throws BookStoreException {
		// Create a rating for a given book
		Set<BookRating> booksToRate = new HashSet<>();
		booksToRate.add(new BookRating(TEST_ISBN, -1));

		//Lets try to add the rating to the store
		client.rateBooks(booksToRate);
	}

	/**
	 *  Test that a book with an invalid ISBN and invalid rating.
	 *	We expect a bookstore exception
	 *
	 */
	@Test(expected = BookStoreException.class)
	public void testRateBookInvalidISBNInvalidRating() throws BookStoreException {
		// Create a rating for a given book
		Set<BookRating> booksToRate = new HashSet<>();
		booksToRate.add(new BookRating(-1, -1));

		//Lets try to add the rating to the store
		client.rateBooks(booksToRate);
	}

	/**
	 *  Test that a book with an invalid ISBN and valid rating.
	 *	We expect a bookstore exception
	 *
	 */
	@Test(expected = BookStoreException.class)
	public void testRateBookInvalidISBNValidRating() throws BookStoreException {
		// Create a rating for a given book
		Set<BookRating> booksToRate = new HashSet<>();
		booksToRate.add(new BookRating(-1, 2));

		//Lets try to add the rating to the store
		client.rateBooks(booksToRate);
	}

	/**
	 * Test that we can get k top rated books from store
	 *
	 */
	@Test
	public void testGetTopRatedBooks() throws BookStoreException {
		// Create and add books with ratings to store
		// Apparently we have a trilogy which gets better by each book
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book1 = new ImmutableStockBook(TEST_ISBN+1, "Test of Thrones 1", "George RR Testin'", (float) 10, 5, 0, 1,
				3, false);
		StockBook book2 = new ImmutableStockBook(TEST_ISBN+2, "Test of Thrones 2", "George RR Testin'", (float) 10, 5, 0, 1,
				4, false);
		StockBook book3 = new ImmutableStockBook(TEST_ISBN+3, "Test of Thrones 3", "George RR Testin'", (float) 10, 5, 0, 1,
				5, false);
		booksToAdd.add(book1);
		booksToAdd.add(book2);
		booksToAdd.add(book3);

		// Add the books to the store through the store manager
		storeManager.addBooks(booksToAdd);

		// Assert that our three top rated books matches the above books
		List<Book> topRatedBooks = client.getTopRatedBooks(3);
		assertTrue(topRatedBooks.contains(book1));
		assertTrue(topRatedBooks.contains(book2));
		assertTrue(topRatedBooks.contains(book3));
	}

	/**
	 * Test that we can get 1 top rated books from store with more than 1 book in store
	 *
	 */
	@Test
	public void testGet1TopRatedBooks() throws BookStoreException {
		// Create and add books with ratings to store
		// Apparently we have a trilogy which gets better by each book

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book1 = new ImmutableStockBook(TEST_ISBN+1, "Test of Thrones 1", "George RR Testin'", (float) 10, 5, 0, 1,
				3, false);
		StockBook book2 = new ImmutableStockBook(TEST_ISBN+2, "Test of Thrones 2", "George RR Testin'", (float) 10, 5, 0, 1,
				4, false);
		StockBook book3 = new ImmutableStockBook(TEST_ISBN+3, "Test of Thrones 3", "George RR Testin'", (float) 10, 5, 0, 1,
				5, false);
		booksToAdd.add(book1);
		booksToAdd.add(book2);
		booksToAdd.add(book3);

		// Add the books to the store through the store manager
		storeManager.addBooks(booksToAdd);

		// Assert that our three top rated books matches the above books
		List<Book> topRatedBooks = client.getTopRatedBooks(1);
		assertTrue(topRatedBooks.contains(book3));
	}

	/**
	 * Test that we can get illformed k in top rated books from store with more than 1 book in store
	 *
	 */
	@Test(expected = BookStoreException.class)
	public void testGetIllformedTopRatedBooks() throws BookStoreException {
		// Create and add books with ratings to store
		// Apparently we have a trilogy which gets better by each book

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book1 = new ImmutableStockBook(TEST_ISBN+1, "Test of Thrones 1", "George RR Testin'", (float) 10, 5, 0, 1,
				3, false);
		StockBook book2 = new ImmutableStockBook(TEST_ISBN+2, "Test of Thrones 2", "George RR Testin'", (float) 10, 5, 0, 1,
				4, false);
		StockBook book3 = new ImmutableStockBook(TEST_ISBN+3, "Test of Thrones 3", "George RR Testin'", (float) 10, 5, 0, 1,
				5, false);
		booksToAdd.add(book1);
		booksToAdd.add(book2);
		booksToAdd.add(book3);

		// Add the books to the store through the store manager
		storeManager.addBooks(booksToAdd);

		// Assert that our three top rated books matches the above books
		List<Book> something = client.getTopRatedBooks(-1);
	}

	/**
	 * Test that we can get illformed k in top rated books from store with zero books in store
	 *
	 */
	@Test(expected = BookStoreException.class)
	public void testGetIllformedZeroBooksInTopRatedBooks() throws BookStoreException {
		List<Book> something = client.getTopRatedBooks(-1);
	}


	/**
	 * Test that we can get all books from store when k is higher than
	 * number of books in store
	 *
	 */
	@Test
	public void testGetAllTopRatedBooks() throws BookStoreException {
		// Create and add books with ratings to store
		// Apparently we have a trilogy which gets better by each book

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book1 = new ImmutableStockBook(TEST_ISBN+1, "Test of Thrones 1", "George RR Testin'", (float) 10, 5, 0, 1,
				3, false);
		StockBook book2 = new ImmutableStockBook(TEST_ISBN+2, "Test of Thrones 2", "George RR Testin'", (float) 10, 5, 0, 1,
				4, false);
		StockBook book3 = new ImmutableStockBook(TEST_ISBN+3, "Test of Thrones 3", "George RR Testin'", (float) 10, 5, 0, 1,
				5, false);
		booksToAdd.add(book1);
		booksToAdd.add(book2);
		booksToAdd.add(book3);

		// Ensure the bookstore is empty
		storeManager.removeAllBooks();

		// Add the books to the store through the store manager
		storeManager.addBooks(booksToAdd);

		// Assert that our three top rated books matches the above books
		List<Book> topRatedBooks = client.getTopRatedBooks(30);
		assertTrue(topRatedBooks.contains(book1));
		assertTrue(topRatedBooks.contains(book2));
		assertTrue(topRatedBooks.contains(book3));
		assertTrue(topRatedBooks.size() == 3);
	}



	/**
	 * Tear down after class.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();

		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
}
