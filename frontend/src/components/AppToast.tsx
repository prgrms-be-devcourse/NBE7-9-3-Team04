"use client";

import { Toaster } from "sonner";

export default function AppToast() {
  return (
    <Toaster
      position="bottom-right"
      richColors
      expand
    />
  );
}