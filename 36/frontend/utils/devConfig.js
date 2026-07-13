/**
 * C 端 API 地址（本地离线运行默认配置）
 *
 * H5 / 浏览器：127.0.0.1 即可
 * 微信小程序：须改为本机局域网 IP（cmd 执行 ipconfig 查看），例如 192.168.1.100
 * 并在微信开发者工具 → 详情 → 本地设置 → 勾选「不校验合法域名」
 *
 * 改完后在 HBuilderX「运行 → 运行到微信开发者工具」重新编译
 */

/** 是否连接本机后端（解压后默认 true，无需改） */
export const USE_LOCAL_BACKEND = true

/** 本机后端地址（H5 用 127.0.0.1；小程序联调请改为局域网 IP:8080） */
const LOCAL_BACKEND = 'http://127.0.0.1:8080'

export const BASE_URL = USE_LOCAL_BACKEND ? LOCAL_BACKEND : 'http://127.0.0.1:8080'
