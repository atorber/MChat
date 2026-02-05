#!/usr/bin/env node
'use strict';
const path = require('path');

const command = process.argv[2];

function showHelp() {
  console.log(`
MoltChat Server - 企业即时通讯服务端

用法: mchat-server [命令]

命令:
  (无)        启动服务端
  db:init     初始化数据库（创建库和表）
  db:seed     创建管理员账号 (employee_id=admin)
  help        显示帮助信息

环境变量:
  CONFIG_PATH  指定配置文件路径（默认 config/config.yaml）

示例:
  # 初始化数据库
  CONFIG_PATH=~/mchat/config.yaml mchat-server db:init

  # 创建管理员
  CONFIG_PATH=~/mchat/config.yaml mchat-server db:seed

  # 启动服务
  CONFIG_PATH=~/mchat/config.yaml mchat-server
`);
}

switch (command) {
  case 'db:init':
    require(path.join(__dirname, '..', 'dist', 'db', 'run-init.js'));
    break;
  case 'db:seed':
    require(path.join(__dirname, '..', 'dist', 'db', 'run-seed-admin.js'));
    break;
  case 'help':
  case '--help':
  case '-h':
    showHelp();
    break;
  default:
    if (command && !command.startsWith('-')) {
      console.error(`未知命令: ${command}`);
      showHelp();
      process.exit(1);
    }
    require(path.join(__dirname, '..', 'dist', 'index.js'));
}
