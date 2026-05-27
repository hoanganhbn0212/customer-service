import { readFileSync, writeFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const dir = dirname(fileURLToPath(import.meta.url));
const file = join(dir, "../src/api/core/OpenAPI.ts");
let content = readFileSync(file, "utf8");
content = content.replace(/BASE:\s*['"][^'"]*['"]/, "BASE: ''");
writeFileSync(file, content);
