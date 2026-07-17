const js = require('@eslint/js');
const {FlatCompat} = require('@eslint/eslintrc');
const prettier = require('eslint-config-prettier');

const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
});

const legacyConfigs = compat.config({
  env: {
    es6: true,
    node: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:import/errors',
    'plugin:import/warnings',
    'plugin:import/typescript',
    'google',
    'plugin:@typescript-eslint/recommended',
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    project: ['tsconfig.json'],
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint', 'import'],
  rules: {
    quotes: ['error', 'single'],
    'import/no-unresolved': 0,
    indent: ['error', 2],
  },
});

const compatibleConfigs = legacyConfigs.map(({rules, ...config}) => {
  const {
    ['valid-jsdoc']: validJsdoc,
    ['require-jsdoc']: requireJsdoc,
    ...compatibleRules
  } = rules || {};
  return {...config, files: ['**/*.ts'], rules: compatibleRules};
});

module.exports = [
  {
    ignores: [
      'lib/**/*', // Ignore built files.
      'generated/**/*', // Ignore generated files.
      'node_modules/**/*',
      'dist/**/*',
      'build/**/*',
    ],
  },
  ...compatibleConfigs,
  prettier,
];
