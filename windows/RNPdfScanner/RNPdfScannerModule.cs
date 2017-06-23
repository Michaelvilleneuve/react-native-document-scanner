using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Com.Reactlibrary.RNPdfScanner
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNPdfScannerModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNPdfScannerModule"/>.
        /// </summary>
        internal RNPdfScannerModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNPdfScanner";
            }
        }
    }
}
