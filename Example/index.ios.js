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
      image: null,
      flashEnabled: false,
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
            enableTorch={this.state.flashEnabled}
            brightness={0}
            saturation={1}
            contrast={1.1}
            style={styles.scanner}
          />
        }
        <Text style={styles.instructions}>
          This is a great example of react-native-pdf-scanner 🤗
        </Text>
        {this.state.image === null ?
          null :
          <TouchableOpacity style={styles.newPic} onPress={() => this.setState({ image: "" })}>
            <Text>Take another picture</Text>
          </TouchableOpacity>
        }

        <TouchableOpacity style={styles.flashEnabledButton} onPress={() => this.setState({ flashEnabled: !this.state.flashEnabled })}>
          <Text>📸 Flash</Text>
        </TouchableOpacity>
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
  newPic: {
    height: 100,
    alignItems: 'center',
    justifyContent: 'center'
  },
  flashEnabledButton: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
    top: 20,
    bottom: 20,
    height: 40,
    width: 120,
    backgroundColor: '#FFF',
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
  scanner: {
    flex: 1,
    width: 400,
    height: 200,
    borderColor: 'orange',
    borderWidth: 1
  }
});

AppRegistry.registerComponent('Example', () => Example);
