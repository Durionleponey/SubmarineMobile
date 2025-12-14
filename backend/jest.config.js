/** @type {import('jest').Config} */
module.exports = {
  // On teste les fichiers dans src/ (c'est le cas standard Nest)
  rootDir: 'src',

  // Fichiers de test unitaires : *.spec.ts
  testRegex: '.*\\.spec\\.ts$',

  // On compile .ts et .js avec ts-jest
  transform: {
    '^.+\\.(t|j)s$': 'ts-jest',
  },

  moduleFileExtensions: ['ts', 'js', 'json'],

  // Couverture (optionnel, mais standard Nest)
  collectCoverageFrom: ['**/*.(t|j)s'],
  coverageDirectory: '../coverage',

  testEnvironment: 'node',
};
