import {onMounted, onUnmounted, ref} from "vue";
import {isMobile} from "@/utils";

export function useScreen() {
  const isPhone = ref(isMobile());

  const fn = () => {
    isPhone.value = isMobile();
  };

  onMounted(() => {
    window.addEventListener("resize", fn);
  });

  onUnmounted(() => {
    window.removeEventListener("resize", fn);
  });

  return {
    isPhone,
  };
}
