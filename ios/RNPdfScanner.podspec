require 'json'

package = JSON.parse(File.read(File.join(__dir__, '../package.json')))

Pod::Spec.new do |s|
  s.name           = 'RNPdfScanner'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = 'https://github.com/Michaelvilleneuve/react-native-document-scanner'
  s.platform       = :ios, '8.0'
  s.source         = { :git => 'https://github.com/Michaelvilleneuve/react-native-document-scanner.git' }
  s.source_files   = 'ios/RNPdfScanner/**/*.{h,m}'
  s.dependency 'React'
  s.requires_arc   = true
end
