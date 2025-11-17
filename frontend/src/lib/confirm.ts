import Swal from "sweetalert2";

export async function confirmAlert(message: string): Promise<boolean> {
  const result = await Swal.fire({
    text: message,
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#d33",
    cancelButtonColor: "#3085d6",
    confirmButtonText: "확인",
    cancelButtonText: "취소",
    customClass: {
      icon: "swal-sm-icon",
    },
  });

  return result.isConfirmed;
}
