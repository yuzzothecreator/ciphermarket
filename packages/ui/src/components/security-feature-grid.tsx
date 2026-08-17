import { Shield, Lock, FileCheck, Eye } from "lucide-react";
import { Badge } from "./badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./card";

const features = [
  {
    icon: Lock,
    title: "Envelope encryption",
    description: "Product assets encrypted at rest with KMS-wrapped keys. Plaintext never stored permanently.",
  },
  {
    icon: FileCheck,
    title: "Verified payments",
    description: "Entitlements granted only after signed server-to-server webhook confirmation.",
  },
  {
    icon: Eye,
    title: "Leak tracing",
    description: "Buyer-specific watermarks and delivery manifests help identify unauthorised distribution sources.",
  },
  {
    icon: Shield,
    title: "Zero-trust delivery",
    description: "Short-lived access grants replace permanent storage URLs for every download.",
  },
];

export function SecurityFeatureGrid() {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      {features.map((feature) => (
        <Card key={feature.title}>
          <CardHeader>
            <div className="mb-2 flex items-center gap-2">
              <feature.icon className="size-5 text-accent" aria-hidden />
              <Badge variant="outline">Security</Badge>
            </div>
            <CardTitle>{feature.title}</CardTitle>
            <CardDescription>{feature.description}</CardDescription>
          </CardHeader>
          <CardContent />
        </Card>
      ))}
    </div>
  );
}
