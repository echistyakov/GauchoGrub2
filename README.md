# Gaucho Grub v2.0
##### Eric Swenson | Andrew Tran | Evgeny Chistyakov

Release practices:
  1. Release builds are ProGuarded (minified) by default.
  2. Unused resources are removed.
  3. Keystore `gaucho_grub_keystore.jks` should be used to sign the release APK.
  4. Password to the keystore is "*password*".

Naming Conventions:
  1. <b>Saving state in activities and fragments:</b> private static final string STATE_XXX = "STATE_XXX" where XXX is the capitalized name of the variable that will be referenced by this tag in the Bundle for saving state.
  2. <b>XML String Resources: </b>Snake case, separated into segments of the strings.xml file via comments like ```<!--- MainActivity Buttons -->```
  3. <b>XML Layout IDs: </b> JavaClass_lowerCamelCaseName where JavaClass is the upper camel case name of the java class the layout belongs to, and lowerCamelCaseName is some identifier for the layout element this ID refers to. 
