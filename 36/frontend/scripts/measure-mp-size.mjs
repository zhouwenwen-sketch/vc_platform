import { readdirSync, statSync } from 'fs'
import { join } from 'path'

function size(dir) {
  let total = 0
  let count = 0
  for (const name of readdirSync(dir)) {
    const p = join(dir, name)
    const st = statSync(p)
    if (st.isDirectory()) {
      const sub = size(p)
      total += sub.total
      count += sub.count
    } else {
      total += st.size
      count += 1
    }
  }
  return { total, count }
}

const targets = [
  'unpackage/dist/dev/mp-weixin',
  'unpackage/dist/build/mp-weixin',
]

for (const t of targets) {
  try {
    const { total, count } = size(t)
    console.log(`${t}: ${(total / 1024).toFixed(1)} KB (${count} files)`)
  } catch (e) {
    console.log(`${t}: not found`)
  }
}
