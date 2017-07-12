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
  Image,
  TouchableOpacity,
  View
} from 'react-native';
import PdfScanner from 'react-native-pdf-scanner';

export default class Example extends Component {
  constructor(props) {
    super(props);
    this.state = {
      image: null
    };
  }

  render() {
    return (
      <View style={styles.container}>
        {this.state.image ?
          <Image style={{ flex: 1, width: 300, height: 200 }} source={{ uri: `data:image/png;base64,${this.state.image}`}} resizeMode="contain" /> :
          <PdfScanner
            onPictureTaken={data => this.setState({ image: data.image })}
            overlayColor="rgba(255,130,0, 0.7)"
            style={{ flex: 1, width: 400, height: 200, borderColor: 'orange', borderWidth: 1 }}
          />
        }
        <Text style={styles.instructions}>
          This is an example of react-native-pdf-scanner 🤗
        </Text>
        {this.state.image === null ?
          null :
          <TouchableOpacity style={{ height: 100, alignItems: 'center', justifyContent: 'center' }} onPress={() => this.setState({ image: "" })}>
            <Text>Take another pic</Text>
          </TouchableOpacity>
        }
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
