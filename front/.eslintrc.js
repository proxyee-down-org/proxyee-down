module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: ['plugin:vue/essential', 'eslint:recommended'],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-alert': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    //强制使用单引号
    quotes: ['error', 'single'],
    //强制不使用分号结尾
    semi: ['error', 'never']
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
  /*
  rules: {
    "prettier/prettier": [
      "error",
      {
        singleQuote: true,
        semi: false,
        parser: "flow"
      }
    ]
    "no-console": process.env.NODE_ENV === "production" ? "error" : "off",
    "no-debugger": process.env.NODE_ENV === "production" ? "error" : "off",
    "no-alert": process.env.NODE_ENV === "production" ? "error" : "off",
    //强制使用单引号
    quotes: [2, "single"],
    //禁止给const变量赋值
    "no-const-assign": "error",
    //不能对var声明的变量使用delete操作符
    "no-delete-var": "error",
    //在创建对象字面量时不允许键重复 {a:1,a:1}
    "no-dupe-keys": "error",
    //函数参数不能重复
    "no-dupe-args": "error",
    //禁止使用eval
    "no-eval": "error",
    //禁止使用隐式eval
    "no-implied-eval": "error",
    //禁止不必要的分号
    "no-extra-semi": "error",
    //switch中的case标签不能重复
    "no-duplicate-case": "error",
    // 禁止 for 循环出现方向错误的循环，比如 for (i = 0; i < 10; i--)
    "for-direction": "error",
    //禁止混用tab和空格
    "no-mixed-spaces-and-tabs": [2, false],
    // 禁止将常量作为 if, for, while 里的测试条件，比如 if (true), for (;;)，除非循环内部有 break 语句
    "no-constant-condition": [
      "error",
      {
        checkLoops: false
      }
    ] 
  },
  */
}
