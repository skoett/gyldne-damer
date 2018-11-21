#ACS - programming 1

**Test ideas for implemented functions:**

**rateBooks:**<br/>
rate book with valid ISBN & valid rating <br/>
rate book with valid ISBN & invalid rating <br/>
rate book with invalid ISBN & valid rating <br/>
rate book with invalid ISBN & invalid rating <br/>
rate multiple books above parameters <br/>
rate multiple books which have identical ISBN's <br/>

**getTopRatedBooks:** <br/>
Get k top rated books with > 1 books in bookstore <br/>
Get k top rated books with 0 books in bookstore <br/>
Get k < n top rated books with n books in bookstore <br/>
Get k > n top rated books with n books in bookstore <br/>
Get 0 top rated books with 0 books in bookstore <br/>
Get 0 top rated books with >= 1 books in bookstore <br/>

**getBooksInDemand:** <br/>
Get books in demand when no books in demand exists <br/>
Get books in demand when there exists books in demand <br/>

**TODO:** <br/>
- Implement getBooksInDemand
- Fix HTTPproxy and HTTPmessangeHandler functions. Should be quick work
- Implement further tests for rateBooks and getTopRatedBooks following the 
  already implemented tests in BookStoreTest.java
- Write tests for getBooksInDemand
