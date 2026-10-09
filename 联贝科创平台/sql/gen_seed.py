import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent
json_path = root / "backend/src/main/resources/config/project-collections.json"
out_path = Path(__file__).resolve().parent / "seed-project-collection-data.sql"

cfg = json.loads(json_path.read_text(encoding="utf-8"))
lines = ["SET NAMES utf8mb4;"]

sort = 0
for tab in cfg.get("tabs", []):
    if tab == "全部":
        continue
    sort += 1
    tab_esc = tab.replace("'", "''")
    lines.append(
        f"INSERT IGNORE INTO project_collection_tab (name, sort_order) VALUES ('{tab_esc}', {sort});"
    )

for sort, item in enumerate(cfg.get("collections", []), 1):
    projects = json.dumps(item.get("projects", []), ensure_ascii=False).replace("'", "''")

    def esc(value):
        if value is None:
            return "NULL"
        return "'" + str(value).replace("'", "''") + "'"

    values = ", ".join(
        [
            str(item["id"]),
            esc(item.get("category")),
            esc(item.get("badge") or None),
            esc(item.get("title")),
            esc(item.get("cover")),
            esc(item.get("coverTitle")),
            esc(item.get("date")),
            esc(item.get("summary")),
            esc(item.get("description")),
            f"'{projects}'",
            str(sort),
            "1",
        ]
    )
    lines.append(
        "INSERT INTO project_collection "
        "(id, category, badge, title, cover, cover_title, collection_date, summary, description, projects_data, sort_order, status) "
        f"VALUES ({values});"
    )

out_path.write_text("\n".join(lines), encoding="utf-8")
print(f"written {out_path} ({len(lines)} statements)")
