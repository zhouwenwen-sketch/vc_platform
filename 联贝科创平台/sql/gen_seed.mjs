import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const jsonPath = path.join(__dirname, '../backend/src/main/resources/config/project-collections.json');
const outPath = path.join(__dirname, 'seed-project-collection-data.sql');
const cfg = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
const lines = ['SET NAMES utf8mb4;'];

let tabSort = 0;
for (const tab of cfg.tabs || []) {
  if (tab === '全部') continue;
  tabSort += 1;
  lines.push(`INSERT IGNORE INTO project_collection_tab (name, sort_order) VALUES ('${tab.replace(/'/g, "''")}', ${tabSort});`);
}

(cfg.collections || []).forEach((item, index) => {
  const sort = index + 1;
  const projects = JSON.stringify(item.projects || []).replace(/'/g, "''");
  const esc = (v) => (v == null || v === '' ? 'NULL' : `'${String(v).replace(/'/g, "''")}'`);
  lines.push(
    'INSERT INTO project_collection (id, category, badge, title, cover, cover_title, collection_date, summary, description, projects_data, sort_order, status) VALUES (' +
      [
        item.id,
        esc(item.category),
        esc(item.badge || null),
        esc(item.title),
        esc(item.cover),
        esc(item.coverTitle),
        esc(item.date),
        esc(item.summary),
        esc(item.description),
        `'${projects}'`,
        sort,
        1,
      ].join(', ') +
      ');'
  );
});

fs.writeFileSync(outPath, lines.join('\n'), 'utf8');
console.log('written', outPath, lines.length);
