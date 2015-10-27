# Webindex tables

 The example uses two tables in Accumulo.  One table is used by the
Fluo component of the example.  The second table stores an index used by the
web application to service queries.

Web pages are the input for the Webindex example and a query table is the
output.  The Fluo table is an intermediate table needed to incrementally keep
the query table up to date.

## Fluo table Schema

TODO

## Query Table Schema

The data in the query table is structured so that the web application can efficiently answer the following questions.

 * Which web page is referenced by the most web pages?
 * For a given domain, which pages in that domain are referenced by the most web pages?
 * For a given web page, what pages reference it?

To answer these questions the query table has three top level row ranges : 
 
 * A row range with prefix `d:` that contains domain information.
 * A row range with prexix `p:` that contains information about individual pages.
 * A row range with prefix `t:` that sorts all URIs by reference count.

### Domain Row Range

In the domain section of the table all rows are of the form `d:<domain>`.  The
following table shows the possible columns for domain rows.

| Family  | Qualifier                 | Timestamp      | Value             | Description 
|---------|---------------------------|----------------|-------------------|-------------
| domain  | pagecount                 | \<export-seq\> | \<num-pages\>     | Count of the number of rank columns in the row
| rank    | \<uri-ref-count\>:\<uri\> | \<export-seq\> | \<uri-ref-count\> | Count of how many times a URI in the domain is referenced.  The count is encoded in the qualifier so that the URI with the most references sorts first.

### Page Row range

In the page section of the table all rows are of the form `p:<uri>`.  The
following table shows the possible columns for page rows.

| Family  | Qualifier         | Timestamp      | Value                   | Description
|---------|-------------------|----------------|-------------------------|------------
| page    | cur               | \<export-seq\> | \<json\>                | The value contains information about a web pages outgoing links encoded in json. 
| page    | incount           | \<export-seq\> | \<uri-ref-count\>       | A count of the number of pages that reference this URI.  This is also the numnber of inlinks column families in this row.
| inlinks | \<uri\>           | \<export-seq\> | \<anchor-text\>         | A URI that references this URI/page.  The value contains the anchor text from the referencing link.

### Total row range

All rows in this range are of the form `t:<uri-ref-count>:<uri>` and there are
no columns.  This row range contains all URIs sorted from most referenced to
least referenced.  The URI reference count in the row is encoded in a special
way to achieve this sort order.

### Example

Input Data :

    a.com/page1 links to c.com, b.com
    b.com links to c.com/page1, c.com
    d.com links to c.com

Resulting Accumulo Table :

    row               cf        cq              value
    --------------------------------------------------
    d:com.a           domain    pagecount       1
                      rank      1:com.a/page1   1
    d:com.b           domain    pagecount       1
                      rank      2:com.b         2
    d:com.c           domain    pagecount       2
                      rank      3:com.c         3
                                1:com.c/page1   1
    d:com.d           domain    pagecount       1
                      rank      1:com.d         1
    p:com.a/page1     page      cur             {"outlinkcount": 2, "outlinks":[c.com, b.com]}
                                incount         0
    p:com.b           inlinks   com.a/pag1      anchorText
                      page      cur             {"outlinkcount": 2, "outlinks":[c.com/page1, c.com]}
                                incount         1
    p:com.c           inlinks   com.a/page1     anchorText
                                com.b           anchorText
                                com.d           anchorText
                      page      incount         3
    p:com.c/page1     inlinks   com.b           anchorText
                      page      incount         1
    p:com.d           page      cur             {"outlinkcount": 1, "outlinks":[c.com]}
                                incount         0
    t:3:com.c                                   3
    t:2:com.b                                   2
    t:1:com.c/page1                             1
    t:0:com.a/page1                             0
    t:0:com.d                                   0
