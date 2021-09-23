# YmlConfigManager
A library that makes converting between yml files and "YmlObjects" easy
<br>
This seriously is a very simple library at the moment, so there may be a lot of features missing. 
<br>
Feel free to comment or anything really

Any class with fields annotated with YcmField could be a Config object
<br>
Pass an instance of this class to a Ycm or YcmDefault

# Documentation
Ycm: the base class to convert between ConfigObjects and files
<br>
YcmField: tags a field in a ConfigObject for when it is converted to a file
<br>
YcmInlineComment/YcmNewlineComment: comments a path in a Yaml file

See <a href="https://github.com/amp7368/YmlConfigManager/wiki">the Wiki</a> for more information

# Getting started
## Example
See <a href="https://github.com/amp7368/YmlConfigManager/blob/master/src/main/java/ycm/yml/manager/example/ExampleYcmConfig.java">ExampleObject</a> for a concrete example
<br>
You can add default values by just adding the defaults in the noArgsConstructor
<br>
YcmDefault.toConfig(file, ExampleYcmConfig.class) will create an object of ExampleYcmConfig from the contents of file

## Gradle
```
repositories {
  // other repositories
  maven { url 'https://jitpack.io' }
}
dependencies {
  // other dependencies
  implementation 'com.github.amp7368:YmlConfigManager:Tag'
}
```
