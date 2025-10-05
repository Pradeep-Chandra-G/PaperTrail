import { useState, useCallback, useMemo } from "react";
import SideBar from "./SideBar";
import CreateNote from "./CreateNote";
import NotesList from "./NotesList";
import AnimatedBar from "./AnimatedBar";
import Dashboard from "./Dashboard";
import ShareNote from "./ShareNote"; // ✅ import

type ViewType =
  | "create"
  | "my-notes"
  | "shared-notes"
  | "dashboard"
  | "share-note";

function MainContent() {
  const [currentView, setCurrentView] = useState<ViewType>("dashboard");
  const [selectedNoteId, setSelectedNoteId] = useState<number>();
  const [sharingNoteId, setSharingNoteId] = useState<number>();
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleMenuClick = useCallback(
    (menu: string) => {
      const viewMap: Record<string, ViewType> = {
        Dashboard: "dashboard",
        "My Notes": "my-notes",
        "Shared Notes": "shared-notes",
      };
      setCurrentView(viewMap[menu] ?? currentView);
      setSelectedNoteId(undefined);
    },
    [currentView]
  );

  const handleNewNote = useCallback(() => {
    setSelectedNoteId(undefined);
    setCurrentView("create");
  }, []);

  const handleSelectNote = useCallback((noteId: number) => {
    setSelectedNoteId(noteId);
    setCurrentView("create");
  }, []);

  const handleNoteSaved = useCallback(() => {
    setRefreshTrigger((prev) => prev + 1);
  }, []);

  const handleShareNote = useCallback((noteId: number) => {
    setSharingNoteId(noteId);
    setCurrentView("share-note");
  }, []);

  const renderedView = useMemo(() => {
    switch (currentView) {
      case "create":
        return <CreateNote noteId={selectedNoteId} onSave={handleNoteSaved} />;
      case "my-notes":
        return (
          <NotesList
            type="my"
            onSelectNote={handleSelectNote}
            onShareNote={handleShareNote} // ✅ pass handler
          />
        );
      case "shared-notes":
        return <NotesList type="shared" onSelectNote={handleSelectNote} />;
      case "share-note":
        return (
          <ShareNote
            noteId={sharingNoteId!}
            onClose={() => setCurrentView("my-notes")}
          />
        );
      case "dashboard":
        return (
          <div className="p-8">
            <Dashboard
              userName="Pradeep"
              labels={["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]}
              notesCreated={[2, 5, 3, 6, 4, 7, 5]}
              notesShared={[1, 2, 1, 3, 2, 4, 3]}
            />
          </div>
        );
    }
  }, [
    currentView,
    selectedNoteId,
    sharingNoteId,
    handleNoteSaved,
    handleSelectNote,
    handleShareNote,
  ]);

  return (
    <div className="flex flex-col md:flex-row justify-around items-stretch md:pt-10 md:pl-15 md:pr-15">
      <div className="w-full sm:w-1/2 md:w-1/3 lg:max-w-sm flex flex-col p-6">
        <SideBar onNewNote={handleNewNote} onMenuClick={handleMenuClick} />
      </div>

      <div className="hidden md:block gap-2">
        <AnimatedBar orientation="vertical" />
      </div>

      <div className="w-full md:pt-8 md:w-[50%] ">{renderedView}</div>
    </div>
  );
}

export default MainContent;
