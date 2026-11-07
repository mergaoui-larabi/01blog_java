export function throttle<Args extends unknown[]>(
  fn: (...args: Args) => void,
  limit: number,
): (...args: Args) => void {
  let inThrottle = false;

  return (...args: Args): void => {
    if (inThrottle) return;
    inThrottle = true;
    fn(...args);
    setTimeout(() => {
      inThrottle = false;
    }, limit);
  };
}
