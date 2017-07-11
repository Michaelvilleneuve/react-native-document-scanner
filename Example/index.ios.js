/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';
import PdfScanner from 'react-native-pdf-scanner';

export default class Example extends Component {
  alert(params) {
    alert('coucou');
    console.log(params);
  }
  render() {
    return (
      <View style={styles.container}>
        <PdfScanner onPictureTaken={this.alert.bind(this)} style={{ flex: 1, width: 400, height: 400, borderColor: 'orange', borderWidth: 1 }} />
        <Text style={styles.instructions}>
          This is an example of react-native-pdf-scanner
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('Example', () => Example);
