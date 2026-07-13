/**
 * Remove unused static assets and compress images for WeChat mini program size limit.
 * Run: node scripts/optimize-static.mjs
 */
import { readdirSync, statSync, unlinkSync, rmSync, existsSync } from 'fs'
import { join, dirname, extname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = join(__dirname, '..')
const staticDir = join(root, 'static')

let sharp
try {
  sharp = (await import('sharp')).default
} catch {
  console.error('Run: npm install sharp --save-dev')
  process.exit(1)
}

  'projectset1.jpg',
  'projectset2.jpg',
  'projectset3.jpg',
  'projectset4.jpg',
  'projectset5.jpg',
  'projectset6.jpg',
]

function walk(dir, files = []) {
  if (!existsSync(dir)) return files
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    const st = statSync(full)
    if (st.isDirectory()) walk(full, files)
    else files.push(full)
  }
  return files
}

function rel(p) {
  return p.replace(/\\/g, '/').slice(root.replace(/\\/g, '/').length + 1)
}

function shouldRemove(filePath) {
  const r = rel(filePath)
  if (REMOVE_GLOBS.some((name) => r === `static/${name}`)) return true
  if (r.startsWith('static/activity/')) return true
  if (r.startsWith('static/home-icons/_source/')) return true
  return false
}

async function compressImage(filePath) {
  const ext = extname(filePath).toLowerCase()
  const before = statSync(filePath).size
  const tmp = `${filePath}.tmp`

  if (ext === '.png') {
    await sharp(filePath)
      .png({ compressionLevel: 9, palette: true, quality: 80 })
      .toFile(tmp)
  } else if (ext === '.jpg' || ext === '.jpeg') {
    await sharp(filePath)
      .jpeg({ quality: 75, mozjpeg: true })
      .toFile(tmp)
  } else {
    return { before, after: before, skipped: true }
  }

  const after = statSync(tmp).size
  if (after < before) {
    const { renameSync } = await import('fs')
    renameSync(tmp, filePath)
    return { before, after, skipped: false }
  }
  unlinkSync(tmp)
  return { before, after: before, skipped: true }
}

async function main() {
  if (!existsSync(staticDir)) {
    console.log('No static/ directory found.')
    return
  }

  const files = walk(staticDir)
  let removedBytes = 0
  let savedBytes = 0

  for (const file of files) {
    if (!shouldRemove(file)) continue
    const size = statSync(file).size
    unlinkSync(file)
    removedBytes += size
    console.log(`REMOVED ${rel(file)} (${(size / 1024).toFixed(1)} KB)`)
  }

  // Remove empty dirs
  for (const sub of ['activity', 'home-icons/_source']) {
    const d = join(staticDir, ...sub.split('/'))
    if (existsSync(d)) {
      try {
        rmSync(d, { recursive: true, force: true })
        console.log(`REMOVED dir static/${sub}`)
      } catch { /* ignore */ }
    }
  }

  const remaining = walk(staticDir)
  for (const file of remaining) {
    const ext = extname(file).toLowerCase()
    if (!['.png', '.jpg', '.jpeg'].includes(ext)) continue
    const { before, after, skipped } = await compressImage(file)
    if (!skipped && after < before) {
      savedBytes += before - after
      console.log(`COMPRESSED ${rel(file)}: ${(before / 1024).toFixed(1)} -> ${(after / 1024).toFixed(1)} KB`)
    }
  }

  const total = walk(staticDir).reduce((s, f) => s + statSync(f).size, 0)
  console.log('')
  console.log(`Removed: ${(removedBytes / 1024).toFixed(1)} KB`)
  console.log(`Compressed savings: ${(savedBytes / 1024).toFixed(1)} KB`)
  console.log(`static/ total now: ${(total / 1024).toFixed(1)} KB`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
