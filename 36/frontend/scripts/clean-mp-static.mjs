/**
 * Remove bloated static assets from mp-weixin build output (after source cleanup).
 * Run after HBuilderX build if upload still fails: node scripts/clean-mp-static.mjs
 */
import { readdirSync, statSync, unlinkSync, rmSync, existsSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = join(__dirname, '..')

const REMOVE_NAMES = new Set([
  'projectset1.jpg', 'projectset2.jpg', 'projectset3.jpg',
  'projectset4.jpg', 'projectset5.jpg', 'projectset6.jpg',
  'banner-demo.png', 'detail-demo.png', 'organizer-ref.png',
])

const BUILD_DIRS = [
  join(root, 'unpackage/dist/dev/mp-weixin'),
  join(root, 'unpackage/dist/build/mp-weixin'),
]

function walk(dir, files = []) {
  if (!existsSync(dir)) return files
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    if (statSync(full).isDirectory()) walk(full, files)
    else files.push(full)
  }
  return files
}

function rel(p) {
  return p.replace(/\\/g, '/')
}

let removed = 0
for (const buildDir of BUILD_DIRS) {
  if (!existsSync(buildDir)) continue
  for (const file of walk(join(buildDir, 'static'))) {
    const name = file.split(/[/\\]/).pop()
    if (REMOVE_NAMES.has(name) || rel(file).includes('/_source/')) {
      const size = statSync(file).size
      unlinkSync(file)
      removed += size
      console.log(`Removed ${rel(file).slice(root.length + 1)} (${(size / 1024).toFixed(1)} KB)`)
    }
  }
  for (const sub of ['static/activity', 'static/home-icons/_source']) {
    const d = join(buildDir, ...sub.split('/'))
    if (existsSync(d)) {
      const size = walk(d).reduce((s, f) => s + statSync(f).size, 0)
      rmSync(d, { recursive: true, force: true })
      removed += size
      console.log(`Removed dir ${rel(d).slice(root.length + 1)} (${(size / 1024).toFixed(1)} KB)`)
    }
  }
}

console.log(`\nTotal removed from build output: ${(removed / 1024).toFixed(1)} KB`)
