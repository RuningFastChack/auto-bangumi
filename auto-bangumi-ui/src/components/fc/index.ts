import { useMountComponent } from '@/hooks/useMountComponent.ts';
import TaskLoadingDialog from '@/components/fc/TaskLoadingDialog.vue';
import ImageViewerDialog from '@/components/fc/ImageViewer.vue';

export async function openLoadingDialog(title: string, text: string, subTitle?: string) {
  return useMountComponent({
    title,
    text,
    subTitle
  }).load<InstanceType<typeof TaskLoadingDialog>>(TaskLoadingDialog);
}

export async function useImageViewerDialog(
  instanceId: string,
  daemonId: string,
  fileName: string,
  frontDir: string
) {
  return useMountComponent({ instanceId, daemonId, fileName, frontDir }).mount(
    ImageViewerDialog
  );
}
