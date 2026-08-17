import * as React from "react";
import { cn } from "../lib/utils";

export function Badge({
  className,
  variant = "default",
  ...props
}: React.HTMLAttributes<HTMLSpanElement> & { variant?: "default" | "accent" | "outline" }) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium",
        variant === "default" && "border-transparent bg-muted text-foreground",
        variant === "accent" && "border-transparent bg-accent/15 text-accent",
        variant === "outline" && "border-border text-muted-foreground",
        className,
      )}
      {...props}
    />
  );
}
