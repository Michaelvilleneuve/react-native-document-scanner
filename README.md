
# React Native Document Scanner

![alt text](https://raw.githubusercontent.com/Michaelvilleneuve/react-native-document-scanner/master/images/mockup.png)

## Getting started

`$ npm install react-native-document-scanner --save`

### Automatic installation

`$ react-native link react-native-document-scanner`

### Manual installation

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-pdf-scanner` and add `RNPdfScanner.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNPdfScanner.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

## Usage
```javascript
import { View, Image } from 'react-native';
import React, { Component } from 'react';
import DocumentScanner from 'react-native-document-scanner';

class YourComponent extends Component {
  render() {
    return (
      <View>
        <DocumentScanner
          onPictureTaken={data => this.setState({ image: data.image })}
          overlayColor="rgba(255,130,0, 0.7)"
          enableTorch={false}
          brightness={0.3}
          saturation={1}
          contrast={1.1}
        />
        <Image source={{ uri: `data:image/png;base64,${this.state.image}`}} resizeMode="contain" />
      </View>
    );
  }
}

```

## Properties

| Prop  | Default  | Type | Description |
| :-------- |:----:| :--------:| :----------|
| overlayColor | `none` | `string` | Color of the detected rectangle : rgba recommended |
| enableTorch | `false` | `bool` | Allows to active or deactivate flash during document detection |
| brightness | `0` | `float` | Increase or decrease camera brightness. Normal as default. |
| saturation | `1` | `float` | Increase or decrease camera saturation. Set `0` for black & white |
| contrast | `1` | `float` | Increase or decrease camera contrast. Normal as default |

## Returned image

| Prop | Params | Type | Description |
| :--- | :----: | :--: | :---------- |
| onPictureTaken | `data` | `object` | Returns the captured image in an object `{ image: 'BASE64 string'}` |
