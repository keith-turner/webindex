
# Code Guide

The Webindex example has three major code components.

 * Spark component :  Generates initial Fluo and Query tables.
 * Fluo component :  Updates the Query table as web pages are added, removed, and updated.
 * Web component : Web application that uses the Query table. 

## Guide to Fluo Component.

The following image shows a high level view of how data flows through the Fluo Webindex code.   

<center>![Observer Map](webindex_graphic.png)</center>
<!--
The image was produced using Google Docs.  A link to the source is here.
https://docs.google.com/drawings/d/1vl26uXtScXn1ssj3WEb-qskuH-15OOmWul1B562oWDc/edit?usp=sharing
-->

### Page Loader
### Page Observer

This observer computes changes to links within a page.  It computes
links added and deleted and then pushes this information to the URI Map
Observer and Page Exporter.

Conceptually when a page references a new URI, a `+1` is queued up for the Uri
Map.  When a page no longer references a URI, a `-1` is queued up for the Uri
Map to process.

**Code:** [PageObserver.java][PageObserver]

### URI Map Observer

The code for this this observer is very simple because it builds on the
Collision Free Map Recipe.  A Collision Free Map has two extension points and
this example implements both.   The first extension point is a
combiner that processes the `+1` and `-1` updates queued up by the Page
Observer.   The second extension point is an update observer the handles
changes in reference counts for a URI.  It pushes these changes in reference
counts to the Domain Map and URI Exporter.

**Code:** [UriMap.java][UriMap]

### Domain Map Observer
### Page Exporter

For each URI, the Query table contains the URIs that reference it.  This export
code keeps that information in the Query table up to date.  One interesting
Fluo concept this highlight is the concept of inversion on export.  The
complete inverted URI index is never built in Fluo, its only built in Query
table.

**Code:** [PageExport.java][PageExport]

### URI Exporter

Previous observers calculated the total number of URIs that reference a URI.
This export code is given the new and old URI reference counts.  URI reference
counts are indexed three different ways in the Query table.  This export code
updates all three places in the Query table.

This export code also uses the invert on export concept.  The three indexes are
never built in the Fluo table.  Fluo only tracks the minimal amount of
information needed to keep the three indexes current.

**Code:** [UriCountExport.java][UriCountExport]

### Domain Exporter


[PageObserver]: ../modules/data/src/main/java/io/fluo/webindex/data/fluo/PageObserver.java
[UriMap]: ../modules/data/src/main/java/io/fluo/webindex/data/fluo/UriMap.java
[UriCountExport]: ../modules/data/src/main/java/io/fluo/webindex/data/fluo/UriCountExport.java
[PageExport]: ../modules/data/src/main/java/io/fluo/webindex/data/fluo/PageExport.java

