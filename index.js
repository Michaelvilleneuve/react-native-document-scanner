import React from 'react';
import { requireNativeComponent } from 'react-native';
import PropTypes from 'prop-types';

const RNPdfScanner = requireNativeComponent('RNPdfScanner', PdfScanner);

class PdfScanner extends React.Component {
  sendOnPictureTakenEvent(event) {
    return this.props.onPictureTaken(event.nativeEvent);
  }
  render() {
    return (
      <RNPdfScanner
        {...this.props}
        onPictureTaken={this.sendOnPictureTakenEvent.bind(this)}
      />
    );
  }
}

PdfScanner.propTypes = {
  onPictureTaken: PropTypes.func,
  overlayColor: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  enableTorch: PropTypes.bool,
  saturation: PropTypes.number,
  brightness: PropTypes.number,
  contrast: PropTypes.number,
};

export default PdfScanner;
