import { writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const dir = dirname(fileURLToPath(import.meta.url));
const frontendRoot = join(dir, "..");
const backend = (process.env.BACKEND_URL || process.env.VITE_BACKEND_URL || "")
  .trim()
  .replace(/\/$/, "");

const rewrites = [];

if (backend) {
  rewrites.push({
    source: "/api/:path*",
    destination: `${backend}/api/:path*`
  });
  console.log(`[vercel] Proxy /api -> ${backend}/api`);
} else {
  console.warn(
    "[vercel] BACKEND_URL chua dat — chi deploy giao dien. Dat bien trong Vercel Settings."
  );
}

rewrites.push({
  source: "/((?!api/).*)",
  destination: "/index.html"
});

const vercelJson = { rewrites };

writeFileSync(join(frontendRoot, "vercel.json"), JSON.stringify(vercelJson, null, 2));
console.log("[vercel] Wrote vercel.json");
