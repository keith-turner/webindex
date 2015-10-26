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

| Family  | Qualifier             | Timestamp    | Value           | 
|---------|-----------------------|--------------|-----------------|
| domain  | pagecount             | <export seq> | <num pages>     |
| rank    | <uri ref count>:<uri> | <export seq> | <uri ref count> |

## Page Row range

In the page section of the table all rows are of the form `p:<uri>`.  The
following table shows the possible columns for page rows.

| Family  | Qualifier         | Timestamp    | Value       | 
|---------|-------------------|--------------|-------------|
| page    | cur               | <export seq> | <pages outlinks json> |
| page    | incount           | <export seq> | <uri ref count> |
| inlinks | <uri>             | <export seq> | <anchor text> |


