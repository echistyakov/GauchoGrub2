# Gaucho Grub v2.0
##### Eric Swenson | Andrew Tran | Evgeny Chistyakov

Release practices:
  1. Release builds are ProGuarded (minified) by default.
  2. Unused resources are removed.
  3. Keystore `gaucho_grub_keystore.jks` should be used to sign the release APK.
  4. Password to the keystore is "*password*".

Naming Conventions:
  1. <b>Saving state in activities and fragments:</b> ```private static final string STATE_XXX = "STATE_XXX";``` where XXX is the capitalized name of the variable that will be referenced by this tag in the Bundle for saving state.
  2. <b>XML String Resources: </b>Snake case, separated into segments of the strings.xml file via comments like ```<!--- MainActivity Buttons -->```
  3. <b>XML Layout IDs: </b> JavaClass_lowerCamelCaseName where JavaClass is the upper camel case name of the java class the layout belongs to, and lowerCamelCaseName is some identifier for the layout element this ID refers to. 

### Acknowledgements: 

Libraries:
 1. [Requery ORM](https://github.com/requery/requery)
 2. [About Libraries](https://github.com/mikepenz/AboutLibraries)
 3. [Joda Time](http://www.joda.org/joda-time/)
 4. [Google Guava](https://github.com/google/guava)
 5. [Google gson](https://github.com/google/gson)
 6. [ButterKnife](http://jakewharton.github.io/butterknife/)
 7. [Material Sheet Fab](https://github.com/gowong/material-sheet-fab)
 8. [OpenCSV](http://opencsv.sourceforge.net/)
 9. [Jsoup](http://jsoup.org/)
 10. [Swipe Layout](https://github.com/daimajia/AndroidSwipeLayout)
 
Icons:
 1. Schedules icon made by [Appzgear](http://www.flaticon.com/authors/appzgear) from [www.flaticon.com](www.flaticon.com)
 2. Fab icon made by [Freepik](http://www.flaticon.com/authors/freepik) from [www.flaticon.com](www.flaticon.com)
 3. Menus icon made by [Freepik](http://www.flaticon.com/authors/freepik) from [www.flaticon.com](www.flaticon.com)
 4. Swipes icon made by [Freepik](http://www.flaticon.com/authors/freepik) from [www.flaticon.com](www.flaticon.com)
 5. Nuts icon made by [Rami McMin](http://www.flaticon.com/authors/rami-mcmin) from [www.flaticon.com](www.flaticon.com)
