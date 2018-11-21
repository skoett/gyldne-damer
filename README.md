ACS - programming 1

Test ideas for implemented functions:

rateBooks:
rate book with valid ISBN & valid rating
rate book with valid ISBN & invalid rating
rate book with invalid ISBN & valid rating
rate book with invalid ISBN & invalid rating
rate multiple books above parameters
rate multiple books which have identical ISBN's

getTopRatedBooks:
Get k top rated books with >1 books in bookstore
Get k top rated books with 0 books in bookstore
Get k<n top rated books with n books in bookstore
Get k>n top rated books with n books in bookstore
Get 0 top rated books with 0 books in bookstore
Get 0 top rated books with >=1 books in bookstore

getBooksInDemand:
Get books in demand when no books in demand exists
Get books in demand when there exists books in demand

TODO:
- Implement getBooksInDemand
- Fix HTTPproxy and HTTPmessangeHandler functions. Should be quick work
- Implement further tests for rateBooks and getTopRatedBooks following the 
  already implemented tests in BookStoreTest.java
- Write tests for getBooksInDemand
