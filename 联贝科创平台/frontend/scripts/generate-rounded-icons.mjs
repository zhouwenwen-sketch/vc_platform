/**
 * Generate soft macaron multi-color icons (iconfont 首页-01 style).
 * Run: node scripts/generate-rounded-icons.mjs
 */
import { mkdirSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = join(__dirname, '..')

let sharp
try {
  sharp = (await import('sharp')).default
} catch {
  console.error('Run: npm install sharp --save-dev')
  process.exit(1)
}

const WHITE = '#FFFFFF'

const PALETTE = {
  active: { primary: '#78B9B1', accent: '#C9E5E1' },
  inactive: { primary: '#BFBFBF', accent: '#E0E0E0' }
}

/** @param {string} body @param {string} accent @param {{primary:string,accent:string}} colors */
function softSvg(body, accent, colors) {
  const apply = (s) => s.replace(/\$\{ACCENT\}/g, colors.accent)
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">
<g fill="${colors.primary}">${apply(body)}</g>
${accent ? apply(accent) : ''}
</svg>`
}

const ICON_DEFS = {
  home: {
    body: `<path d="M32 9c.9 0 1.8.2 2.6.6l17.5 11c1.1.7 1.9 2 1.9 3.4v23.5c0 3-2.4 5.5-5.5 5.5H15.5c-3 0-5.5-2.5-5.5-5.5V24c0-1.4.8-2.7 1.9-3.4L29.4 9.6c.8-.4 1.7-.6 2.6-.6z"/>
<rect x="19" y="25" width="10" height="7" rx="3.5" fill="${WHITE}"/>
<path d="M27.5 35.5a4.5 4.5 0 0 1 9 0V50h-9V35.5z" fill="${WHITE}"/>`,
    accent: `<rect x="43" y="13" width="7" height="12" rx="3.5" fill="\${ACCENT}"/>`
  },
  project: {
    body: `<rect x="11" y="20" width="42" height="30" rx="10"/>
<path d="M19 20v-4a5 5 0 0 1 5-5h16a5 5 0 0 1 5 5v4"/>
<rect x="21" y="30" width="22" height="5" rx="2.5" fill="${WHITE}"/>
<rect x="21" y="39" width="14" height="5" rx="2.5" fill="${WHITE}"/>`,
    accent: `<rect x="39" y="9" width="9" height="13" rx="4.5" fill="\${ACCENT}"/>`
  },
  activity: {
    body: `<rect x="9" y="15" width="46" height="40" rx="11"/>
<rect x="9" y="24" width="46" height="7" rx="3" fill="${WHITE}" opacity=".55"/>
<rect x="18" y="9" width="5" height="12" rx="2.5"/>
<rect x="41" y="9" width="5" height="12" rx="2.5"/>
<path d="M21 37l5.5 5.5L43 26" stroke="${WHITE}" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>`,
    accent: ''
  },
  mine: {
    body: `<circle cx="32" cy="21" r="11"/>
<path d="M11 53c0-11.5 9.4-19 21-19s21 7.5 21 19v3H11v-3z"/>`,
    accent: `<circle cx="45" cy="16" r="5" fill="\${ACCENT}"/>`
  },
  // 融资快报：折角文档
  news: {
    body: `<path d="M13 9h24a5 5 0 0 1 5 5v40a5 5 0 0 1-5 5H13a5 5 0 0 1-5-5V14a5 5 0 0 1 5-5z"/>
<path d="M37 9v10a4 4 0 0 0 4 4h10" fill="\${ACCENT}"/>
<rect x="18" y="22" width="20" height="4" rx="2" fill="${WHITE}"/>
<rect x="18" y="31" width="20" height="4" rx="2" fill="${WHITE}"/>
<rect x="18" y="40" width="13" height="4" rx="2" fill="${WHITE}"/>`,
    accent: ''
  },
  // 融资事件：日历
  events: {
    body: `<rect x="9" y="14" width="46" height="42" rx="11"/>
<rect x="9" y="23" width="46" height="7" fill="${WHITE}" opacity=".55"/>
<rect x="17" y="8" width="5" height="13" rx="2.5"/>
<rect x="42" y="8" width="5" height="13" rx="2.5"/>
<circle cx="21" cy="35" r="3.5" fill="${WHITE}"/>
<circle cx="32" cy="35" r="3.5" fill="${WHITE}"/>
<circle cx="43" cy="35" r="3.5" fill="${WHITE}"/>
<circle cx="21" cy="45" r="3.5" fill="${WHITE}"/>
<circle cx="32" cy="45" r="3.5" fill="${WHITE}"/>`,
    accent: ''
  },
  // 项目库：文件夹
  library: {
    body: `<path d="M10 22a5 5 0 0 1 5-5h12l4 4h18a5 5 0 0 1 5 5v20a5 5 0 0 1-5 5H15a5 5 0 0 1-5-5V22z"/>
<rect x="18" y="30" width="28" height="5" rx="2.5" fill="${WHITE}"/>
<rect x="18" y="39" width="18" height="5" rx="2.5" fill="${WHITE}"/>`,
    accent: `<rect x="44" y="14" width="7" height="9" rx="3.5" fill="\${ACCENT}"/>`
  },
  // 机构库：办公楼
  institution: {
    body: `<rect x="14" y="18" width="36" height="36" rx="8"/>
<rect x="20" y="10" width="24" height="12" rx="6"/>
<rect x="20" y="28" width="7" height="7" rx="2" fill="${WHITE}"/>
<rect x="37" y="28" width="7" height="7" rx="2" fill="${WHITE}"/>
<rect x="20" y="40" width="7" height="7" rx="2" fill="${WHITE}"/>
<rect x="37" y="40" width="7" height="7" rx="2" fill="${WHITE}"/>
<path d="M28 40a4 4 0 0 1 8 0v14h-8V40z" fill="${WHITE}"/>`,
    accent: ''
  },
  // 项目集：四宫格
  collection: {
    body: `<rect x="10" y="11" width="19" height="19" rx="6"/>
<rect x="35" y="11" width="19" height="19" rx="6"/>
<rect x="10" y="34" width="19" height="19" rx="6"/>
<rect x="35" y="34" width="19" height="19" rx="6"/>
<circle cx="19.5" cy="20.5" r="3" fill="${WHITE}"/>
<circle cx="44.5" cy="20.5" r="3" fill="${WHITE}"/>
<circle cx="19.5" cy="43.5" r="3" fill="${WHITE}"/>
<circle cx="44.5" cy="43.5" r="3" fill="${WHITE}"/>`,
    accent: ''
  },
  // 研究院：打开的书
  research: {
    body: `<path d="M10 16c0-3 2-5 5-5h14v38H15c-3 0-5-2-5-5V16z"/>
<path d="M54 16c0-3-2-5-5-5H35v38h14c3 0 5-2 5-5V16z"/>
<path d="M24 11v38" stroke="${WHITE}" stroke-width="3" stroke-linecap="round"/>
<rect x="14" y="22" width="8" height="3" rx="1.5" fill="${WHITE}"/>
<rect x="42" y="22" width="8" height="3" rx="1.5" fill="${WHITE}"/>`,
    accent: `<circle cx="48" cy="13" r="5" fill="\${ACCENT}"/>`
  },
  // 投资人认证：人物+盾牌
  investor: {
    body: `<circle cx="26" cy="22" r="10"/>
<path d="M8 50c0-9 8-15 18-15s18 6 18 15v2H8v-2z"/>
<path d="M42 14h12a4 4 0 0 1 4 4v10a12 12 0 0 1-8 11.3V14z"/>
<path d="M48 24l3 3 6-7" stroke="${WHITE}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" fill="none"/>`,
    accent: ''
  },
  // 项目入驻：加号圆
  onboard: {
    body: `<circle cx="32" cy="32" r="23"/>
<rect x="29" y="19" width="6" height="26" rx="3" fill="${WHITE}"/>
<rect x="19" y="29" width="26" height="6" rx="3" fill="${WHITE}"/>`,
    accent: `<circle cx="49" cy="15" r="5.5" fill="\${ACCENT}"/>`
  },
  // 寻求报道：麦克风
  coverage: {
    body: `<rect x="27" y="10" width="10" height="22" rx="5"/>
<path d="M18 28a14 14 0 0 0 28 0" stroke="\${ACCENT}" stroke-width="4" fill="none" stroke-linecap="round"/>
<rect x="29" y="40" width="6" height="10" rx="3"/>
<rect x="22" y="48" width="20" height="5" rx="2.5"/>`,
    accent: ''
  },
  // FA服务：人民币符号
  fa: {
    body: `<circle cx="32" cy="32" r="23"/>
<path d="M22 24h20M22 32h16M22 40h20" stroke="${WHITE}" stroke-width="4" stroke-linecap="round"/>
<path d="M36 20v24M28 28c4-4 8-4 12 0s-8 8-12 8" stroke="${WHITE}" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" fill="none"/>`,
    accent: `<circle cx="49" cy="15" r="5.5" fill="\${ACCENT}"/>`
  },
  // 融资并购：双向箭头
  ma: {
    body: `<rect x="9" y="20" width="18" height="24" rx="7"/>
<rect x="37" y="20" width="18" height="24" rx="7"/>
<path d="M27 32h10" stroke="${WHITE}" stroke-width="4" stroke-linecap="round"/>
<path d="M33 28l4 4-4 4M31 28l-4 4 4 4" stroke="${WHITE}" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>`,
    accent: ''
  }
}

async function svgToPng(svg, size, outPath) {
  await sharp(Buffer.from(svg)).resize(size, size).png().toFile(outPath)
}

async function renderSoft(name, colors, size, outPath) {
  const def = ICON_DEFS[name]
  await svgToPng(softSvg(def.body, def.accent, colors), size, outPath)
}

async function main() {
  const tabDir = join(root, 'static/tabbar')
  const homeDir = join(root, 'static/home-icons')
  mkdirSync(tabDir, { recursive: true })
  mkdirSync(homeDir, { recursive: true })

  const tabIcons = ['home', 'project', 'activity', 'mine']
  for (const name of tabIcons) {
    await renderSoft(name, PALETTE.inactive, 81, join(tabDir, `${name}.png`))
    await renderSoft(name, PALETTE.active, 81, join(tabDir, `${name}-active.png`))
  }

  const menuKeys = [
    'news', 'events', 'library', 'institution', 'collection',
    'research', 'investor', 'onboard', 'coverage', 'fa', 'ma'
  ]
  for (const key of menuKeys) {
    await renderSoft(key, PALETTE.active, 88, join(homeDir, `${key}.png`))
  }

  console.log('Generated soft macaron icons')
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
