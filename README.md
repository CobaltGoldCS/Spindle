# Spindle (Deprecated in favor of [Spindler](https://github.com/CobaltGoldCS/Spindler))
An app to simplify web scraping for books and other 'written' media

## Configurations
Add configurations to the configuration screen to inform the app what to scrape.
### Supports css paths __and__ xpaths
#### Css paths
If you prefix a normal css path with a __$__, it will select that attribute.
For example ```$text .givencsspathhere``` will select the text specifically of the _first_ tag selected by the csspath.
#### X Paths (in testing; not available for public use)
Spindle supports x paths for selecting text and href _directly_ 
