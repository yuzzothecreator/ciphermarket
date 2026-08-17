import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  transpilePackages: ["@ciphermarket/ui", "@ciphermarket/contracts"],
  outputFileTracingRoot: path.join(__dirname, "../.."),
};

export default nextConfig;
