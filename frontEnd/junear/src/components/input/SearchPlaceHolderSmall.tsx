import { ReactComponent as SearchIcon } from '../../assets/image/search-icon.svg';

function SearchPlaceHolderSmall() {
  return (
    <>
      <div className="w-full h-[60px] bg-zinc-700 rounded-[10px] border-b border-neutral-400 flex items-center">
        <input
          type="text"
          placeholder="회사명을 입력해 주세요"
          className="text-neutral-400 text-base text-left pl-4 outline-none bg-transparent w-full"
        />
        <div className="p-4 justify-end">
          <SearchIcon />
        </div>
      </div>
    </>
  );
}

export default SearchPlaceHolderSmall;
