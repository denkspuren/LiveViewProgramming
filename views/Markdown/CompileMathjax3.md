# How to compile Mathjax3

To manually compile the Mathjax3 library a working installation of node and npm is required.

## Clone the Repo

```
git clone https://github.com/tani/markdown-it-mathjax3.git
```

## Install Dependencies

```
cd markdown-it-mathjax3
npm install
```

## Browserify

```
npx browserify index.js --standalone mathjax3 > mathjax3.js
```

## Further Reading

- [Mathjax3](https://github.com/tani/markdown-it-mathjax3)
- [Browserify](https://browserify.org/index.html)
- [Browserify Handbook](https://github.com/browserify/browserify-handbook?tab=readme-ov-file#standalone)
