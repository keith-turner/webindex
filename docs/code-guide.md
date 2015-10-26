
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

This observer computes the changes to links within a page.  Specifically it computes the links added and deleted and then initiates follow on processing.   

**Code:** [PageObserver.java][PageObserver]

### URI Map Observer
### Domain Map Observer
### Page Export
### URI Export
### Domain Export


[PageObserver]: ../modules/data/src/main/java/io/fluo/webindex/data/fluo/PageObserver.java

