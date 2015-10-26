# Webindex Query Table Schema

Web pages are the input for the Webindex example and the query table is the output.  The data in the query table is structured so that the web application can efficiently answer the following questions.

 * Which web page is referenced by the most web pages?
 * For a given domain, which pages in that domain are referenced by the most web pages?
 * For a given web page, what pages reference it?

To answer these questions the query table has three top level row ranges : 
 
 * A row range with prefix `d:` that contains domain information.
 * A row range with prexix `p:` that contains information about individual pages.
 * A row range with prefix `t:` that sorts all URIs by reference count.

## Domain Row Range

In the domain section of the table all rows are of the form `d:<domain>`.  The
following table shows the possible columns for domain rows.

| Family  | Qualifier                 | Timestamp      | Value             | Description 
|---------|---------------------------|----------------|-------------------|-------------
| domain  | pagecount                 | \<export-seq\> | \<num-pages\>     | Count of the number of rank columns in the row
| rank    | \<uri-ref-count\>:\<uri\> | \<export-seq\> | \<uri-ref-count\> | Count of how many times a URI in the domain is referenced.  The count is encoded in the qualifier so that the URI with the most references sorts first.

## Page Row range

In the page section of the table all rows are of the form `p:<uri>`.  The
following table shows the possible columns for page rows.

| Family  | Qualifier         | Timestamp      | Value                   | Description
|---------|-------------------|----------------|-------------------------|------------
| page    | cur               | \<export-seq\> | \<json\>                | The value contains information about a web pages outgoing links encoded in json. 
| page    | incount           | \<export-seq\> | \<uri-ref-count\>       | A count of the number of pages that reference this URI.  This is also the numnber of inlinks column families in this row.
| inlinks | \<uri\>           | \<export-seq\> | \<anchor-text\>         | A URI that references this URI/page.  The value contains the anchor text from the referencing link.

## Total row range

All rows in this range are of the form `t:<uri-ref-count>:<uri>` and there are
no columns.  This row range contains all URIs sorted from most referenced to
least referenced.  The URI reference count in the row is encoded in a special
way to achieve this sort order.






