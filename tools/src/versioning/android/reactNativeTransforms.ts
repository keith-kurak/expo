import escapeRegExp from 'lodash/escapeRegExp';
import path from 'path';

import { FileTransforms } from '../../Transforms.types';
import { JniLibNames } from './libraries';
import { packagesToRename } from './packagesConfig';

function pathFromPkg(pkg: string): string {
  return pkg.replace(/\./g, '/');
}

export function reactNativeTransforms(
  versionedReactNativeRoot: string,
  abiVersion: string
): FileTransforms {
  return {
    path: [],
    content: [
      // Update codegen folder to our customized folder
      {
        paths: './ReactAndroid/build.gradle',
        find: /"REACT_GENERATED_SRC_DIR=.+?",/,
        replaceWith: `"REACT_GENERATED_SRC_DIR=${versionedReactNativeRoot}",`,
      },
      // Add generated java to sourceSets
      {
        paths: './ReactAndroid/build.gradle',
        find: /(\bsrcDirs = \["src\/main\/java",.+)(\])/,
        replaceWith: `$1, "${path.join(versionedReactNativeRoot, 'codegen')}/java"$2`,
      },
      // Disable codegen plugin
      {
        paths: './ReactAndroid/build.gradle',
        find: /(\bid\("com\.facebook\.react"\)$)/m,
        replaceWith: '// $1',
      },
      {
        paths: './ReactAndroid/build.gradle',
        find: /(^react {[^]+?\n\})/m,
        replaceWith: '/* $1 */',
      },
      {
        paths: './ReactAndroid/build.gradle',
        find: /(\bpreBuild\.dependsOn\("generateCodegenArtifactsFromSchema"\))/,
        replaceWith: '// $1',
      },
      ...packagesToRename.map((pkg: string) => ({
        paths: [
          './ReactCommon/**/*.{java,h,cpp,mk}',
          './ReactAndroid/src/main/**/*.{java,h,cpp,mk}',
        ],
        find: new RegExp(`${escapeRegExp(pathFromPkg(pkg))}`, 'g'),
        replaceWith: `${abiVersion}/${pathFromPkg(pkg)}`,
      })),
      {

      }
    ],
  };
}

export function codegenTransforms(abiVersion: string): FileTransforms {
  return {
    path: [],
    content: [
      ...packagesToRename.map((pkg: string) => ({
        paths: ['**/*.{java,h,cpp,mk}'],
        find: new RegExp(`${escapeRegExp(pathFromPkg(pkg))}`, 'g'),
        replaceWith: `${abiVersion}/${pathFromPkg(pkg)}`,
      })),
    ],
  };
}
