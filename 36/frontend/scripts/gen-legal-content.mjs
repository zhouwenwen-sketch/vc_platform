import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const contentDir = path.join(__dirname, '../content')

function toParagraphs(raw, title, updateDate, effectiveDate, navTitle) {
  const blocks = raw
    .replace(/\r/g, '')
    .split(/\n\s*\n/)
    .map((s) => s.trim())
    .filter(
      (s) =>
        s &&
        s !== '登录' &&
        !s.startsWith('消息通知') &&
        !s.startsWith('咨询入驻') &&
        !s.startsWith('扫描下方二维码') &&
        !s.startsWith('商务合作')
    )
    .filter((s) => !/^大学生创投(用户服务协议|隐私政策|平台)$/.test(s))
    .filter((s) => !/^更新日期：/.test(s))
    .filter((s) => !/^生效日期：/.test(s))

  const paragraphs = blocks

  return { navTitle, title, updateDate, effectiveDate, paragraphs }
}

const userRaw = fs.readFileSync(path.join(contentDir, 'user-agreement.source.txt'), 'utf8')
const privacyRaw = fs.readFileSync(path.join(contentDir, 'privacy-policy.source.txt'), 'utf8')

const user = toParagraphs(
  userRaw,
  '大学生创投用户服务协议',
  '2023年08月02日',
  '2023年08月02日',
  '大学生创投用户服务协议'
)
const privacy = toParagraphs(
  privacyRaw,
  '大学生创投隐私政策',
  '2025年06月17日',
  '2025年06月17日',
  '大学生创投平台'
)

fs.writeFileSync(
  path.join(contentDir, 'user-agreement.js'),
  `export const USER_AGREEMENT = ${JSON.stringify(user, null, 2)}\n`
)
fs.writeFileSync(
  path.join(contentDir, 'privacy-policy.js'),
  `export const PRIVACY_POLICY = ${JSON.stringify(privacy, null, 2)}\n`
)

console.log('generated', user.paragraphs.length, privacy.paragraphs.length)
