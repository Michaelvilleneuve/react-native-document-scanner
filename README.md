
# react-native-pdf-scanner

## Getting started

`$ npm install react-native-pdf-scanner --save`

### Automatic installation

`$ react-native link react-native-pdf-scanner`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-pdf-scanner` and add `RNPdfScanner.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNPdfScanner.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

## Usage
```javascript
import PdfScanner from 'react-native-pdf-scanner';

...

  return (<PdfScanner />);
```
